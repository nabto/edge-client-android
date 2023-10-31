package com.nabto.edge.iamutil;

import android.util.Log;

import com.nabto.edge.iamutil.PairingMode;
import com.nabto.edge.client.*;
import com.nabto.edge.client.impl.ConnectionImpl;

import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * IMPORTANT: If you run the tests without the following, they will all fail with NabtoChannelException!!
 * use run_all.sh to start up the local devices
 *   ./local_test_device/run_all.sh
 * IMPORTANT: the run_all.sh script only runs on macos
 * Then run the tests in a separate terminal
 *   ./gradlew iam-util:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.nabto.edge.iamutil.IamTest
 * Shorthand:
 *   ./gradlew iam-util:cAT
 */


@RunWith(AndroidJUnit4.class)
public class IamTest {
    private NabtoClient client;
    private Connection connection;
    private SynchronousQueue<Integer> _queue;

    private String localInitialAdminKey =
        "-----BEGIN EC PRIVATE KEY-----\n" +
        "MHcCAQEEIAl3ZURem5NMCTZA0OeTPcT7y6T2FHjHhmQz54UiH7mQoAoGCCqGSM49\n" +
        "AwEHoUQDQgAEbiabrII+WZ8ABD4VQpmLe3cSIWdQfrRbxXotx5yxwInfgLuDU+rq\n" +
        "OIFReqTf5h+Nwp/jj00fnsII88n1YCveoQ==\n" +
        "-----END EC PRIVATE KEY-----\n";

    private void resolve() {
        try {
            _queue.take();
        } catch (Exception e) {
            Log.e("IamTest", "Failed to resolve - will not throw as this crashes Nabto Client SDK native thread with Swig error with no trace of what went wrong: " + e);
        }
    }

    private void await() {
        try {
            _queue.put(0);
        } catch (Exception e) {
            fail("Failed to await()");
        }
    }

    @Before
    public void setup() {
        client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        connection = null;
        _queue = new SynchronousQueue<>();
    }

    private void cleanup(Connection conn) {
        conn.connectionClose();
        // invoke AutoCloseable's cleanup to reclaim locks etc
        conn.close();
    }

    @After
    public void teardown() {
        if (connection != null) {
            cleanup(connection);
            connection = null;
        }
        if (client != null) {
            client.stop();
            client = null;
        }
        for (int i=0; i<10; i++) {
            Runtime.getRuntime().gc();
            try {
                Thread.sleep(10);
            } catch (Exception ignore) {
            }
        }
    }

    class LocalDevice {
        public final String productId;
        public final String deviceId;
        public final String serverUrl;
        public final String serverKey;
        public final String sct;
        public final String password;

        public LocalDevice(
            String productId,
            String deviceId,
            String serverUrl,
            String serverKey,
            String sct,
            String password) {
                this.productId = productId;
                this.deviceId = deviceId;
                this.serverUrl = serverUrl;
                this.serverKey = serverKey;
                this.sct = sct;
                this.password = password;
        }

        public String json(String privateKey) {
            JSONObject result = new JSONObject();
            try {
                result
                    .put("Local", true)
                    .put("ProductId", productId)
                    .put("DeviceId", deviceId)
                    .put("ServerUrl", serverUrl)
                    .put("ServerKey", serverKey)
                    .put("PrivateKey", privateKey);
                if (sct != null) {
                    result.put("ServerConnectToken", sct);
                }
            } catch (JSONException e) {
                return "{}";
            }
            return result.toString();
        }
    }

    private LocalDevice localPasswordPairingDisabledConfig = new LocalDevice(
        "pr-fatqcwj9",
        "de-y3qyrjsn",
        "https://pr-fatqcwj9.clients.nabto.net",
        "sk-9c826d2ebb4343a789b280fe22b98305",
        null,
        "pff3wUnbs7V7"
    );

    private LocalDevice localPairPasswordOpen = new LocalDevice(
        "pr-fatqcwj9",
        "de-aiywxrjr",
        "https://pr-fatqcwj9.clients.nabto.net",
        "sk-9c826d2ebb4343a789b280fe22b98305",
        "RTLRgFXLwCsk",
        "pUhkiHnLhaoo"
    );

    private LocalDevice localPairLocalOpen = new LocalDevice(
        "pr-fatqcwj9",
        "de-ysymtcbh",
        "https://pr-fatqcwj9.clients.nabto.net",
        "sk-9c826d2ebb4343a789b280fe22b98305",
        null,
        ""
    );

    private LocalDevice localPairLocalInitial = new LocalDevice(
        "pr-fatqcwj9",
        "de-i9dqsmif",
        "https://pr-fatqcwj9.clients.nabto.net",
        "sk-9c826d2ebb4343a789b280fe22b98305",
        null,
        ""
    );

    private LocalDevice localPasswordInvite = new LocalDevice(
        "pr-fatqcwj9",
        "de-vma9qrox",
        "https://pr-fatqcwj9.clients.nabto.net",
        "sk-9c826d2ebb4343a789b280fe22b98305",
        null,
        "buKVmisdxETM"
    );

    private LocalDevice localMdnsDevice = new LocalDevice(
        "pr-mdns",
        "de-mdns",
        "https://pr-fatqcwj9.clients.nabto.net",
        "none",
        "none",
        ""
    );

    private interface Function {
        void run();
    }

    // We dont have access to JUnit assertThrows, here's an alternative with the exact same interface.
    private <T extends Throwable> T assertThrows(Class<T> expectedType, Function fun) {
        Throwable e = null;
        try {
            fun.run();
        } catch (Throwable exception) {
            e = exception;
        }
        if (e == null) {
            fail("Expected Exception to be thrown");
        }
        assertEquals(expectedType, e.getClass());
        return (T)e;
    }

    private void assertIamError(IamError error, Function fun) {
        Throwable e = null;
        try {
            fun.run();
        } catch (Throwable exception) {
            e = exception;
        }
        if (e == null) {
            fail("Expected IamException to be thrown");
        }
        assertEquals(com.nabto.edge.iamutil.IamException.class, e.getClass());
        com.nabto.edge.iamutil.IamException iamEx = (IamException)e;
        assertEquals(iamEx.getError(), error);
    }

    private Connection connectToDevice(LocalDevice dev) {
        Connection connection = client.createConnection();
        String pk = client.createPrivateKey();
        connection.updateOptions(dev.json(pk));
        try {
            connection.connect();
        } catch (NabtoNoChannelsException e) {
            fail(e.getLocalChannelErrorCode().getName());
        }
        return connection;
    }

    private Connection connectToDeviceWithAdminKey(LocalDevice dev) {
        Connection connection = client.createConnection();
        connection.updateOptions(dev.json(localInitialAdminKey));
        connection.connect();

        IamUtil iam = IamUtil.create();
        String initialUser = "admin";
        String tmpUser = uniqueUser();
        IamUser currentUser = null;
        try {
            currentUser = iam.getCurrentUser(connection);
        } catch (Exception e) {
            currentUser = null;
        }

        if (currentUser != null) {
            assertEquals(currentUser.getUsername(), initialUser);
            try {
                iam.renameUser(connection, initialUser, tmpUser);
                iam.createUser(connection, initialUser, "", "Administrator");
                iam.deleteUser(connection, tmpUser);
            } catch (Exception e) {
                fail("Failed to reset LocalInitial pairing state");
            }
        }

        return connection;
    }

    private String uniqueUser() {
        return UUID.randomUUID().toString().toLowerCase().substring(0, 16);
    }

    @Test
    public void testPasswordOpenSuccess() {
        connection = connectToDevice(localPairPasswordOpen);
        IamUtil iam = IamUtil.create();
        iam.pairPasswordOpen(connection, uniqueUser(), localPairPasswordOpen.password);
    }

    @Test
    public void testPasswordOpenSuccessCallback() throws Exception {
        connection = connectToDevice(localPairPasswordOpen);
        IamUtil iam = IamUtil.create();
        AtomicInteger errorCode = new AtomicInteger(-1);
        CountDownLatch latch = new CountDownLatch(1);
        iam.pairPasswordOpenCallback(connection, uniqueUser(), localPairPasswordOpen.password, (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch.countDown();
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.NONE.ordinal(), errorCode.get());
    }

    @Test
    public void testPasswordOpenWrongPassword() {
        connection = connectToDevice(localPairPasswordOpen);
        IamUtil iam = IamUtil.create();
        assertIamError(IamError.AUTHENTICATION_ERROR, () -> {
            iam.pairPasswordOpen(connection, uniqueUser(), "i-am-a-clown-with-a-wrong-password");
        });
    }

    @Test
    public void testPasswordOpenWrongPasswordCallback() throws Exception {
        connection = connectToDevice(localPairPasswordOpen);
        IamUtil iam = IamUtil.create();
        AtomicInteger errorCode = new AtomicInteger(-1);
        CountDownLatch latch = new CountDownLatch(1);
        iam.pairPasswordOpenCallback(connection, uniqueUser(), "i-am-a-clown-with-a-wrong-password", (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch.countDown();
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.AUTHENTICATION_ERROR.ordinal(), errorCode.get());
    }

    @Test
    public void testPasswordOpenBlockedByConfig() {
        LocalDevice dev = localPasswordPairingDisabledConfig;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();
        assertIamError(IamError.BLOCKED_BY_DEVICE_CONFIGURATION, () -> {
            iam.pairPasswordOpen(connection, uniqueUser(), dev.password);
        });
    }

    @Test
    public void testPasswordOpenBlockedByConfigCallback() throws Exception {
        LocalDevice dev = localPasswordPairingDisabledConfig;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();
        AtomicInteger errorCode = new AtomicInteger(-1);
        CountDownLatch latch = new CountDownLatch(1);
        iam.pairPasswordOpenCallback(connection, uniqueUser(), dev.password, (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch.countDown();
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.BLOCKED_BY_DEVICE_CONFIGURATION.ordinal(), errorCode.get());
    }

    @Test
    public void testLocalOpenSuccess() {
        connection = connectToDevice(localPairLocalOpen);
        IamUtil iam = IamUtil.create();

        assertFalse(iam.isCurrentUserPaired(connection));
        iam.pairLocalOpen(connection, uniqueUser());
        assertTrue(iam.isCurrentUserPaired(connection));
    }

    @Test
    public void testLocalOpenSuccessCallback() throws InterruptedException {
        connection = connectToDevice(localPairLocalOpen);
        IamUtil iam = IamUtil.create();
        AtomicInteger errorCode0 = new AtomicInteger(-1);
        AtomicInteger errorCode1 = new AtomicInteger(-1);
        AtomicBoolean result0 = new AtomicBoolean(true);
        AtomicBoolean result1 = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        iam.isCurrentUserPairedCallback(connection, (ec0, res0) -> {
            errorCode0.set(ec0.ordinal());
            result0.set(res0.get());
            iam.pairLocalOpenCallback(connection, uniqueUser(), (ec1, res1) -> {
                iam.isCurrentUserPairedCallback(connection, (ec2, res2) -> {
                    errorCode1.set(ec2.ordinal());
                    result1.set(res2.get());
                    latch.countDown();
                });
            });
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));

        assertEquals(IamError.NONE.ordinal(), errorCode0.get());
        assertFalse(result0.get());

        assertEquals(IamError.NONE.ordinal(), errorCode1.get());
        assertTrue(result1.get());
    }

    @Test
    public void testLocalOpenInvalidUsername() {
        connection = connectToDevice(localPairLocalOpen);
        IamUtil iam = IamUtil.create();
        assertIamError(IamError.INVALID_INPUT, () -> {
            iam.pairLocalOpen(connection, "Worst username in the history of usernames");
        });
    }

    @Test
    public void testLocalOpenInvalidUsernameCallback() throws InterruptedException {
        connection = connectToDevice(localPairLocalOpen);
        IamUtil iam = IamUtil.create();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger errorCode = new AtomicInteger(-1);
        iam.pairLocalOpenCallback(connection, "Worst username in the history of usernames", (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch.countDown();
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.INVALID_INPUT.ordinal(), errorCode.get());
    }

    @Test
    public void testLocalOpenUsernameAlreadyExists() {
        LocalDevice dev = localPairLocalOpen;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();
        String username = uniqueUser();
        iam.pairLocalOpen(connection, username);
        assertIamError(IamError.USERNAME_EXISTS, () -> {
            iam.pairLocalOpen(connection, username);
        });
    }

    @Test
    public void testLocalOpenUsernameAlreadyExistsCallback() throws InterruptedException {
        LocalDevice dev = localPairLocalOpen;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();
        String username = uniqueUser();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger errorCode = new AtomicInteger(-1);

        iam.pairLocalOpenCallback(connection, username, (_ec, _res) -> {
            iam.pairLocalOpenCallback(connection, username, (ec, res) -> {
                errorCode.set(ec.ordinal());
                latch.countDown();
            });
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.USERNAME_EXISTS.ordinal(), errorCode.get());
    }

    @Test
    public void testLocalOpenBlockedByConfig() {
        connection = connectToDevice(localPasswordPairingDisabledConfig);
        IamUtil iam = IamUtil.create();
        assertIamError(IamError.PAIRING_MODE_DISABLED, () -> {
            iam.pairLocalOpen(connection, uniqueUser());
        });
    }

    @Test
    public void testLocalOpenBlockedByConfigCallback() throws InterruptedException {
        connection = connectToDevice(localPasswordPairingDisabledConfig);
        IamUtil iam = IamUtil.create();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger errorCode = new AtomicInteger(-1);

        iam.pairLocalOpenCallback(connection, uniqueUser(), (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch.countDown();
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.PAIRING_MODE_DISABLED.ordinal(), errorCode.get());
    }

    @Test
    public void testPasswordInviteSuccess() {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        String admin = uniqueUser();
        iam.pairPasswordOpen(connection, admin, dev.password);

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        iam.createUser(connection, guest, guestPassword, "Guest");
        iam.getUser(connection, guest);

        // Reconnect as user instead of admin
        cleanup(connection);
        connection = connectToDevice(dev);
        assertFalse(iam.isCurrentUserPaired(connection));
        iam.pairPasswordInvite(connection, guest, guestPassword);
        assertTrue(iam.isCurrentUserPaired(connection));
    }

    @Test
    public void testPasswordInviteSuccessCallback() throws Exception {
        client.setLogLevel("trace");
        LocalDevice dev = localPasswordInvite;
        IamUtil iam = IamUtil.create();
        final AtomicInteger errorCode = new AtomicInteger(-1);
        String guest = uniqueUser();
        String guestPassword = "guestpassword";

        try (Connection connection = connectToDevice(dev)) {
            CountDownLatch adminLatch = new CountDownLatch(1);
            String admin = uniqueUser();
            iam.pairPasswordOpenCallback(connection, admin, dev.password, (ec, res) -> {
                errorCode.set(ec.ordinal());
                adminLatch.countDown();
            });
            assertTrue(adminLatch.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.ordinal(), errorCode.get());

            CountDownLatch userCreateLatch = new CountDownLatch(1);
            iam.createUserCallback(connection, guest, guestPassword, "Guest", (ec, res) -> {
                errorCode.set(ec.ordinal());
                userCreateLatch.countDown();
            });
            assertTrue(userCreateLatch.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.ordinal(), errorCode.get());

            CountDownLatch userGetLatch = new CountDownLatch(1);
            iam.getUserCallback(connection, guest, (ec, res) -> {
                errorCode.set(ec.ordinal());
                userGetLatch.countDown();
            });
            assertEquals(IamError.NONE.ordinal(), errorCode.get());

            connection.connectionClose();
        }

        // Reconnect as user instead of admin
        try (Connection connection = connectToDevice(dev)) {
            CountDownLatch isPairedLatch1 = new CountDownLatch(1);
            AtomicBoolean isPaired = new AtomicBoolean(true);
            iam.isCurrentUserPairedCallback(connection, (ec, res) -> {
                errorCode.set(ec.ordinal());
                isPaired.set(res.get());
                isPairedLatch1.countDown();
            });
            assertTrue(isPairedLatch1.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.name(), IamError.values()[errorCode.get()].name());

            assertFalse(isPaired.get());

            CountDownLatch pairLatch = new CountDownLatch(1);
            iam.pairPasswordInviteCallback(connection, guest, guestPassword, (ec, res) -> {
                errorCode.set(ec.ordinal());
                pairLatch.countDown();
            });
            assertTrue(pairLatch.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.name(), IamError.values()[errorCode.get()].name());

            CountDownLatch isPairedLatch2 = new CountDownLatch(1);
            iam.isCurrentUserPairedCallback(connection, (ec, res) -> {
                isPaired.set(res.get());
                errorCode.set(ec.ordinal());
                isPairedLatch2.countDown();
            });

            assertTrue(isPairedLatch2.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.name(), IamError.values()[errorCode.get()].name());
            assertTrue(isPaired.get());
            connection.connectionClose();
        }
    }


    @Test
    public void testPasswordInviteWrongUser() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        String admin = uniqueUser();
        iam.pairPasswordOpen(connection, admin, dev.password);

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        iam.createUser(connection, guest, guestPassword, "Guest");
        iam.getUser(connection, guest);

        // Reconnect as user instead of admin
        cleanup(connection);
        // Have to make a new variables to capture in lambda, java is really great...
        Connection connectionUser = connectToDevice(dev);
        assertFalse(iam.isCurrentUserPaired(connectionUser));
        assertIamError(IamError.AUTHENTICATION_ERROR, () -> {
            iam.pairPasswordInvite(connectionUser, "bonk", guestPassword);
        });
        cleanup(connectionUser);
    }

    @Test
    public void testPasswordInviteWrongUserCallback() throws InterruptedException {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        String admin = uniqueUser();
        String guest = uniqueUser();
        String guestPassword = "guestpassword";

        CountDownLatch latch0 = new CountDownLatch(1);
        final AtomicInteger errorCode = new AtomicInteger(-1);
        iam.pairPasswordOpenCallback(connection, admin, dev.password, (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch0.countDown();
        });
        assertTrue(latch0.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.NONE.name(), IamError.values()[errorCode.get()].name());

        CountDownLatch latch1 = new CountDownLatch(1);
        errorCode.set(-1);
        iam.createUserCallback(connection, guest, guestPassword, "Guest", (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch1.countDown();
        });
        assertTrue(latch1.await(1, TimeUnit.SECONDS));
        assertEquals(errorCode.get(), IamError.NONE.ordinal());

        CountDownLatch latch2 = new CountDownLatch(1);
        errorCode.set(-1);
        iam.getUserCallback(connection, guest, (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch2.countDown();
        });
        assertTrue(latch2.await(1, TimeUnit.SECONDS));
        assertEquals(errorCode.get(), IamError.NONE.ordinal());

        CountDownLatch latch3 = new CountDownLatch(1);
        errorCode.set(-1);
        final AtomicBoolean result = new AtomicBoolean(true);
        // Reconnect as user instead of admin
        cleanup(connection);
        Connection connectionUser = connectToDevice(dev);
        iam.isCurrentUserPairedCallback(connectionUser, (ec, res) -> {
            errorCode.set(ec.ordinal());
            if (ec == IamError.NONE && res.isPresent()) {
                result.set(res.get());
            }
            latch3.countDown();
        });
        assertTrue(latch3.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.NONE.ordinal(), errorCode.get());
        assertFalse(result.get());

        CountDownLatch latch4 = new CountDownLatch(1);
        errorCode.set(-1);
        iam.pairPasswordInviteCallback(connectionUser, "bonk", guestPassword, (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch4.countDown();
        });
        assertTrue(latch4.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.AUTHENTICATION_ERROR.ordinal(), errorCode.get());
        cleanup(connectionUser);
    }

    @Test
    public void testCreateUserBadRole() {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        String admin = uniqueUser();
        iam.pairPasswordOpen(connection, admin, dev.password);

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        assertIamError(IamError.ROLE_DOES_NOT_EXIST, () -> {
            iam.createUser(connection, guest, guestPassword, "Clown");
        });
    }

    @Test
    public void testCreateUserBadRoleCallback() throws InterruptedException {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        CountDownLatch latch0 = new CountDownLatch(1);
        AtomicInteger errorCode = new AtomicInteger(-1);
        String admin = uniqueUser();

        iam.pairPasswordOpenCallback(connection, admin, dev.password, (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch0.countDown();
        });
        assertTrue(latch0.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.NONE.ordinal(), errorCode.get());

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        CountDownLatch latch1 = new CountDownLatch(1);
        iam.createUserCallback(connection, guest, guestPassword, "Clown", (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch1.countDown();
        });
        assertTrue(latch1.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.ROLE_DOES_NOT_EXIST.ordinal(), errorCode.get());
    }

    @Test
    public void testCheckUnpairedUser() {
        connection = connectToDevice(localPasswordInvite);
        IamUtil iam = IamUtil.create();
        assertFalse(iam.isCurrentUserPaired(connection));
        assertIamError(IamError.USER_DOES_NOT_EXIST, () -> {
            iam.getCurrentUser(connection);
        });
    }

    @Test
    public void testCheckUnpairedUserCallback() throws InterruptedException {
        connection = connectToDevice(localPasswordInvite);
        IamUtil iam = IamUtil.create();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger errorCode = new AtomicInteger(-1);
        AtomicBoolean result = new AtomicBoolean(true);

        iam.isCurrentUserPairedCallback(connection, (ec0, res0) -> {
            result.set(res0.get());
            iam.getCurrentUserCallback(connection, (ec1, res1) -> {
                errorCode.set(ec1.ordinal());
                latch.countDown();
            });
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.USER_DOES_NOT_EXIST.ordinal(), errorCode.get());
        assertFalse(result.get());
    }

    @Test
    public void testCheckPairedUserNoIamSupport() {
        connection = connectToDevice(localMdnsDevice);
        IamUtil iam = IamUtil.create();
        assertIamError(IamError.IAM_NOT_SUPPORTED, () -> {
            iam.isCurrentUserPaired(connection);
        });
    }

    @Test
    public void testCheckPairedUserNoIamSupportCallback() throws InterruptedException {
        connection = connectToDevice(localMdnsDevice);
        IamUtil iam = IamUtil.create();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger errorCode = new AtomicInteger(-1);

        iam.isCurrentUserPairedCallback(connection, (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch.countDown();
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.IAM_NOT_SUPPORTED.ordinal(), errorCode.get());
    }

    @Test
    public void testCreateUserAndGetUser() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();
        String admin = uniqueUser();
        iam.pairPasswordOpen(connection, admin, dev.password);

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        iam.createUser(connection, guest, guestPassword, "Guest");

        // Reconnect as user instead of admin
        cleanup(connection);
        Connection connectionUser = connectToDevice(dev);
        assertFalse(iam.isCurrentUserPaired(connectionUser));
        iam.pairPasswordInvite(connectionUser, guest, guestPassword);
        assertTrue(iam.isCurrentUserPaired(connectionUser));

        // Guest is not allowed to GET admin
        assertIamError(IamError.BLOCKED_BY_DEVICE_CONFIGURATION, () -> {
            iam.getUser(connectionUser, admin);
        });

        // Guest can GET self
        IamUser me = iam.getUser(connectionUser, guest);
        assertNotNull(me);
        assertEquals(me.getUsername(), guest);
        assertEquals(me.getRole(), "Guest");

        cleanup(connectionUser);
    }

    @Test
    public void testCreateUserAndGetUserCallback() throws InterruptedException {
        LocalDevice dev = localPasswordInvite;
        IamUtil iam = IamUtil.create();
        String admin = uniqueUser();
        String guest = uniqueUser();
        String guestPassword = "guestpassword";

        try (Connection adminConnection = connectToDevice(dev)) {
            iam.pairPasswordOpen(adminConnection, admin, dev.password);
            iam.createUser(adminConnection, guest, guestPassword, "Guest");

            // Reconnect as user instead of admin
            cleanup(adminConnection);
        }

        CountDownLatch latch0 = new CountDownLatch(1);
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(1);
        CountDownLatch latch4 = new CountDownLatch(1);

        AtomicInteger errorCode = new AtomicInteger(-1);
        AtomicBoolean result0 = new AtomicBoolean(true);
        AtomicBoolean result2 = new AtomicBoolean(false);
        AtomicReference<String> newUser = new AtomicReference<String>();
        AtomicReference<String> newRole = new AtomicReference<String>();

        try (Connection userConnection = connectToDevice(dev)) {
            iam.isCurrentUserPairedCallback(userConnection, (ec, res) -> {
                errorCode.set(ec.ordinal());
                result0.set(res.get());
                latch0.countDown();
            });
            assertTrue(latch0.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.ordinal(), errorCode.get());
            assertFalse(result0.get());

            errorCode.set(-1);
            iam.pairPasswordInviteCallback(userConnection, guest, guestPassword, (ec, res) -> {
                errorCode.set(ec.ordinal());
                latch1.countDown();
            });
            assertTrue(latch1.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.ordinal(), errorCode.get());

            errorCode.set(-1);
            iam.isCurrentUserPairedCallback(userConnection, (ec, res) -> {
                errorCode.set(ec.ordinal());
                result2.set(res.get());
                latch2.countDown();
            });
            assertTrue(latch2.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.ordinal(), errorCode.get());
            assertTrue(result2.get());

            // Guest cannot GET admin
            errorCode.set(-1);
            iam.getUserCallback(userConnection, admin, (ec, res) -> {
                errorCode.set(ec.ordinal());
                latch3.countDown();
            });
            assertTrue(latch3.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.BLOCKED_BY_DEVICE_CONFIGURATION.ordinal(), errorCode.get());

            // Guest can GET self
            errorCode.set(-1);
            iam.getUserCallback(userConnection, guest, (ec, opt) -> {
                errorCode.set(ec.ordinal());
                IamUser me = opt.get();
                newUser.set(me.getUsername());
                newRole.set(me.getRole());
                latch4.countDown();
            });
            assertTrue(latch4.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.ordinal(), errorCode.get());
            assertEquals(guest, newUser.get());
            assertEquals("Guest", newRole.get());
            userConnection.connectionClose();
        }
    }

    @Test
    public void testSetDisplayName() {
        LocalDevice dev = localPairLocalOpen;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        String username = uniqueUser();
        String displayName = uniqueUser();

        iam.pairLocalOpen(connection, username);
        iam.updateUserDisplayName(connection, username, displayName);

        IamUser user = iam.getCurrentUser(connection);
        assertEquals(user.username, username);
        assertEquals(user.displayName, displayName);
    }

    @Test
    public void testSetDisplayNameCallback() throws InterruptedException {
        LocalDevice dev = localPairLocalOpen;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        String username = uniqueUser();
        String displayName = uniqueUser();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger errorCode = new AtomicInteger(-1);

        iam.pairLocalOpen(connection, username);
        iam.updateUserDisplayNameCallback(connection, username, displayName, (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch.countDown();
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.NONE.ordinal(), errorCode.get());

        CountDownLatch latch2 = new CountDownLatch(1);
        AtomicReference<String> deviceUsername = new AtomicReference<String>();
        AtomicReference<String> deviceDisplayName = new AtomicReference<String>();
        errorCode.set(-1);

        iam.getCurrentUserCallback(connection, (ec, opt) -> {
            errorCode.set(ec.ordinal());
            IamUser user = opt.get();
            deviceUsername.set(user.getUsername());
            deviceDisplayName.set(user.getDisplayName());
            latch2.countDown();
        });
        assertTrue(latch2.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.NONE.ordinal(), errorCode.get());
        assertEquals(username, deviceUsername.get());
        assertEquals(displayName, deviceDisplayName.get());
    }

    @Test
    public void testDeleteUser() {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        String admin = uniqueUser();
        iam.pairPasswordOpen(connection, admin, dev.password);
        assertTrue(iam.isCurrentUserPaired(connection));

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        iam.createUser(connection, guest, guestPassword, "Guest");

        IamUser guestUser = iam.getUser(connection, guest);
        assertEquals(guestUser.getUsername(), guest);
        assertEquals(guestUser.getRole(), "Guest");

        assertIamError(IamError.USER_DOES_NOT_EXIST, () -> {
            iam.deleteUser(connection, "nonexistent");
        });

        iam.deleteUser(connection, guest);

        assertIamError(IamError.USER_DOES_NOT_EXIST, () -> {
            iam.getUser(connection, guest);
        });
    }

    @Test
    public void testDeleteUserCallback() throws InterruptedException {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        String admin = uniqueUser();
        iam.pairPasswordOpen(connection, admin, dev.password);
        assertTrue(iam.isCurrentUserPaired(connection));

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        iam.createUser(connection, guest, guestPassword, "Guest");

        IamUser guestUser = iam.getUser(connection, guest);
        assertEquals(guestUser.getUsername(), guest);
        assertEquals(guestUser.getRole(), "Guest");

        CountDownLatch latch0 = new CountDownLatch(1);
        AtomicInteger errorCode = new AtomicInteger(-1);

        iam.deleteUserCallback(connection, "nonexistent", (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch0.countDown();
        });
        assertTrue(latch0.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.USER_DOES_NOT_EXIST.ordinal(), errorCode.get());

        CountDownLatch latch1 = new CountDownLatch(1);
        errorCode.set(-1);

        iam.deleteUserCallback(connection, guest, (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch1.countDown();
        });
        assertTrue(latch1.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.NONE.ordinal(), errorCode.get());

        assertIamError(IamError.USER_DOES_NOT_EXIST, () -> {
            iam.getUser(connection, guest);
        });
    }

    @Test
    public void testLocalInitialSuccess() {
        connection = connectToDeviceWithAdminKey(localPairLocalInitial);
        IamUtil iam = IamUtil.create();
        assertFalse(iam.isCurrentUserPaired(connection));
        iam.pairLocalInitial(connection);
        assertTrue(iam.isCurrentUserPaired(connection));
    }

    @Test
    public void testLocalInitialSuccessCallback() throws InterruptedException {
        connection = connectToDeviceWithAdminKey(localPairLocalInitial);
        IamUtil iam = IamUtil.create();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger errorCode0 = new AtomicInteger(-1);
        AtomicInteger errorCode1 = new AtomicInteger(-1);
        AtomicInteger errorCode2 = new AtomicInteger(-1);
        AtomicBoolean result0 = new AtomicBoolean(true);
        AtomicBoolean result2 = new AtomicBoolean(false);

        iam.isCurrentUserPairedCallback(connection, (ec0, res0) -> {
            errorCode0.set(ec0.ordinal());
            result0.set(res0.get());
            iam.pairLocalInitialCallback(connection, (ec1, res1) -> {
                errorCode1.set(ec1.ordinal());
                iam.isCurrentUserPairedCallback(connection, (ec2, res2) -> {
                    errorCode2.set(ec2.ordinal());
                    result2.set(res2.get());
                    latch.countDown();
                });
            });
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.NONE.ordinal(), errorCode0.get());
        assertFalse(result0.get());
        assertEquals(IamError.NONE.ordinal(), errorCode1.get());
        assertEquals(IamError.NONE.ordinal(), errorCode2.get());
        assertTrue(result2.get());
    }

    @Test
    public void testLocalInitialAlreadyPairedFail() {
        connection = connectToDeviceWithAdminKey(localPairLocalInitial);
        IamUtil iam = IamUtil.create();
        assertFalse(iam.isCurrentUserPaired(connection));
        iam.pairLocalInitial(connection);
        assertTrue(iam.isCurrentUserPaired(connection));
        assertIamError(IamError.INITIAL_USER_ALREADY_PAIRED, () -> {
            iam.pairLocalInitial(connection);
        });
    }

    @Test
    public void testLocalInitialAlreadyPairedFailCallback() throws InterruptedException {
        connection = connectToDeviceWithAdminKey(localPairLocalInitial);
        IamUtil iam = IamUtil.create();

        CountDownLatch latch0 = new CountDownLatch(1);
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(1);

        AtomicInteger errorCode = new AtomicInteger(-1);
        AtomicBoolean result = new AtomicBoolean(true);

        iam.isCurrentUserPairedCallback(connection, (ec, res) -> {
            errorCode.set(ec.ordinal());
            result.set(res.get());
            latch0.countDown();
        });
        assertTrue(latch0.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.NONE.ordinal(), errorCode.get());
        assertFalse(result.get());

        errorCode.set(-1);
        iam.pairLocalInitialCallback(connection, (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch1.countDown();
        });
        assertTrue(latch1.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.NONE.ordinal(), errorCode.get());

        errorCode.set(-1);
        result.set(false);
        iam.isCurrentUserPairedCallback(connection, (ec, res) -> {
            errorCode.set(ec.ordinal());
            result.set(res.get());
            latch2.countDown();
        });
        assertTrue(latch2.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.NONE.ordinal(), errorCode.get());
        assertTrue(result.get());

        errorCode.set(-1);
        iam.pairLocalInitialCallback(connection, (ec, res) -> {
            errorCode.set(ec.ordinal());
            latch3.countDown();
        });
        assertTrue(latch3.await(1, TimeUnit.SECONDS));
        assertEquals(IamError.INITIAL_USER_ALREADY_PAIRED.ordinal(), errorCode.get());
    }

    @Test
    public void testGetDeviceDetails() {
        LocalDevice dev = localPairLocalInitial;
        connection = connectToDeviceWithAdminKey(dev);
        IamUtil iam = IamUtil.create();
        DeviceDetails details = iam.getDeviceDetails(connection);
        assertEquals(details.getProductId(), dev.productId);
        assertEquals(details.getDeviceId(), dev.deviceId);
        assertArrayEquals(details.getModes(), new String[] { "LocalInitial" });
    }

    @Test
    public void testGetDeviceDetailsCallback() {
        LocalDevice dev = localPairLocalInitial;
        connection = connectToDeviceWithAdminKey(dev);
        IamUtil iam = IamUtil.create();

        CompletableFuture<DeviceDetails> future = new CompletableFuture<>();
        iam.getDeviceDetailsCallback(connection, (error, value) -> {
            assertEquals(error, IamError.NONE);
            future.complete(value.get());
        });

        try {
            DeviceDetails details = future.get();
            assertEquals(details.getProductId(), dev.productId);
            assertEquals(details.getDeviceId(), dev.deviceId);
            assertArrayEquals(details.getModes(), new String[] { "LocalInitial" });
        } catch (Exception e) {
            fail("Asynchronous test failed with exception " + e.toString());
        }
    }

    @Test
    public void testGetPairingModes1() {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        PairingMode[] modes = iam.getAvailablePairingModes(connection);
        assertEquals(modes.length, 2);

        List<PairingMode> modesList = Arrays.asList(modes);
        assertTrue(modesList.contains(PairingMode.PASSWORD_INVITE));
        assertTrue(modesList.contains(PairingMode.PASSWORD_OPEN));
    }

    @Test
    public void testGetPairingModes2() {
        LocalDevice dev = localPairLocalInitial;
        connection = connectToDeviceWithAdminKey(dev);
        IamUtil iam = IamUtil.create();

        PairingMode[] modes = iam.getAvailablePairingModes(connection);
        assertEquals(modes.length, 1);

        List<PairingMode> modesList = Arrays.asList(modes);
        assertTrue(modesList.contains(PairingMode.LOCAL_INITIAL));
    }

    @Test
    public void testGetPairingModes3() {
        LocalDevice dev = localPairLocalOpen;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        PairingMode[] modes = iam.getAvailablePairingModes(connection);
        assertEquals(modes.length, 1);

        List<PairingMode> modesList = Arrays.asList(modes);
        assertTrue(modesList.contains(PairingMode.LOCAL_OPEN));
    }

    private String createAdminAndGuest(Connection connection, LocalDevice dev, String guestPassword) {
        String admin = uniqueUser();
        IamUtil iam = IamUtil.create();
        iam.pairPasswordOpen(connection, admin, dev.password);

        String user = uniqueUser();
        iam.createUser(connection, user, guestPassword, "Guest");

        return user;
    }

    @Test
    public void testUpdateUserPassword() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        String guestPassword = "guestpassword";
        IamUtil iam = IamUtil.create();

        String user = createAdminAndGuest(connection, dev, guestPassword);
        String newGuestPassword = "newguestpassword";

        // Should fail
        assertIamError(IamError.USER_DOES_NOT_EXIST, () -> {
            iam.updateUserPassword(connection, "baduser", "sus");
        });

        // Shouldn't fail
        iam.updateUserPassword(connection, user, newGuestPassword);

        // Reconnect as user instead of admin
        cleanup(connection);
        Connection connectionUser = connectToDevice(dev);
        assertFalse(iam.isCurrentUserPaired(connectionUser));

        assertIamError(IamError.AUTHENTICATION_ERROR, () -> {
            iam.pairPasswordInvite(connectionUser, user, guestPassword);
        });
        iam.pairPasswordInvite(connectionUser, user, newGuestPassword);
        assertTrue(iam.isCurrentUserPaired(connectionUser));
        cleanup(connectionUser);
    }

    @Test
    public void testUpdateUserPasswordCallback() throws InterruptedException {
        LocalDevice dev = localPasswordInvite;
        String guestPassword = "guestpassword";
        IamUtil iam = IamUtil.create();

        CountDownLatch latch0 = new CountDownLatch(1);
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(1);
        CountDownLatch latch4 = new CountDownLatch(1);
        CountDownLatch latch5 = new CountDownLatch(1);
        AtomicInteger errorCode = new AtomicInteger(-1);
        AtomicBoolean result = new AtomicBoolean(true);

        String user;
        String newGuestPassword = "newguestpassword";

        try (Connection connection = connectToDevice(dev)) {
             user = createAdminAndGuest(connection, dev, guestPassword);

            // Should fail
            iam.updateUserPasswordCallback(connection, "baduser", "sus", (ec, res) -> {
                errorCode.set(ec.ordinal());
                latch0.countDown();
            });
            assertTrue(latch0.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.USER_DOES_NOT_EXIST.ordinal(), errorCode.get());

            errorCode.set(-1);
            iam.updateUserPasswordCallback(connection, user, newGuestPassword, (ec, res) -> {
                errorCode.set(ec.ordinal());
                latch1.countDown();
            });
            assertTrue(latch1.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.ordinal(), errorCode.get());

            connection.connectionClose();
        }

        try (Connection connection = connectToDevice(dev)) {
            errorCode.set(-1);
            iam.isCurrentUserPairedCallback(connection, (ec, res) -> {
                errorCode.set(ec.ordinal());
                result.set(res.get());
                latch2.countDown();
            });
            assertTrue(latch2.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.ordinal(), errorCode.get());
            assertFalse(result.get());

            errorCode.set(-1);
            iam.pairPasswordInviteCallback(connection, user, guestPassword, (ec, res) -> {
                errorCode.set(ec.ordinal());
                latch3.countDown();
            });
            assertTrue(latch3.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.AUTHENTICATION_ERROR.ordinal(), errorCode.get());

            errorCode.set(-1);
            iam.pairPasswordInviteCallback(connection, user, newGuestPassword, (ec, res) -> {
                errorCode.set(ec.ordinal());
                latch4.countDown();
            });
            assertTrue(latch4.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.ordinal(), errorCode.get());

            errorCode.set(-1);
            result.set(false);
            iam.isCurrentUserPairedCallback(connection, (ec, res) -> {
                errorCode.set(ec.ordinal());
                result.set(res.get());
                latch5.countDown();
            });
            assertTrue(latch5.await(1, TimeUnit.SECONDS));
            assertEquals(IamError.NONE.ordinal(), errorCode.get());
            assertTrue(result.get());

            connection.connectionClose();
        }
    }

    @Test
    public void testUpdateUserRole() {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        String guestPassword = "guestpassword";
        IamUtil iam = IamUtil.create();

        String user = createAdminAndGuest(connection, dev, guestPassword);
        String newRole = "Standard";

        // Should fail
        assertIamError(IamError.ROLE_DOES_NOT_EXIST, () -> {
            iam.updateUserRole(connection, user, "badrole");
        });

        // Shouldn't fail
        iam.updateUserRole(connection, user, newRole);

        IamUser userHandle = iam.getUser(connection, user);
        assertEquals(userHandle.getRole(), newRole);
    }

    @Test
    public void testUpdateUserRoleCallback() {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        String guestPassword = "guestpassword";
        IamUtil iam = IamUtil.create();

        String user = createAdminAndGuest(connection, dev, guestPassword);
        String newRole = "Standard";

        // Should fail
        iam.updateUserRoleCallback(connection, user, "badrole", (ec, res) -> {
            assertEquals(ec, IamError.ROLE_DOES_NOT_EXIST);
            resolve();
        });
        await();

        // Shouldn't fail
        iam.updateUserRoleCallback(connection, user, newRole, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();

        iam.getUserCallback(connection, user, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            IamUser userHandle = res.get();
            assertEquals(userHandle.getRole(), newRole);
            resolve();
        });
        await();
    }

    @Test
    public void testUpdateUserDisplayName() {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        String guestPassword = "guestpassword";
        IamUtil iam = IamUtil.create();

        String user = createAdminAndGuest(connection, dev, guestPassword);
        String newDisplayName = "Morbius";

        // Should fail
        assertIamError(IamError.USER_DOES_NOT_EXIST, () -> {
            iam.updateUserDisplayName(connection, "morb", newDisplayName);
        });

        // Shouldn't fail
        iam.updateUserDisplayName(connection, user, newDisplayName);

        IamUser userHandle = iam.getUser(connection, user);
        assertEquals(userHandle.getDisplayName(), newDisplayName);
    }

    @Test
    public void testUpdateUserDisplayNameCallback() {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        String guestPassword = "guestpassword";
        IamUtil iam = IamUtil.create();

        String user = createAdminAndGuest(connection, dev, guestPassword);
        String newDisplayName = "Morbius";

        // Should fail
        iam.updateUserDisplayNameCallback(connection, "morb", newDisplayName, (ec, res) -> {
            assertEquals(ec, IamError.USER_DOES_NOT_EXIST);
            resolve();
        });
        await();

        // Shouldn't fail
        iam.updateUserDisplayNameCallback(connection, user, newDisplayName, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();

        iam.getUserCallback(connection, user, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            IamUser userHandle = res.get();
            assertEquals(userHandle.getDisplayName(), newDisplayName);
            resolve();
        });
        await();
    }

    @Test
    public void testRenameUser() {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        String guestPassword = "guestpassword";
        IamUtil iam = IamUtil.create();

        String user = createAdminAndGuest(connection, dev, guestPassword);
        String newUsername = uniqueUser();

        // Should fail
        assertIamError(IamError.USER_DOES_NOT_EXIST, () -> {
            iam.renameUser(connection, "idontexist", newUsername);
        });

        // Shouldn't fail
        iam.renameUser(connection, user, newUsername);

        IamUser userHandle = iam.getUser(connection, newUsername);
        assertEquals(userHandle.getUsername(), newUsername);
    }

    @Test
    public void testRenameUserCallback() {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        String guestPassword = "guestpassword";
        IamUtil iam = IamUtil.create();

        String user = createAdminAndGuest(connection, dev, guestPassword);
        String newUsername = uniqueUser();

        // Should fail
        iam.renameUserCallback(connection, "idontexist", newUsername, (ec, res) -> {
            assertEquals(ec, IamError.USER_DOES_NOT_EXIST);
            resolve();
        });
        await();

        // Shouldn't fail
        iam.renameUserCallback(connection, user, newUsername, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();

        iam.getUserCallback(connection, newUsername, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            IamUser userHandle = res.get();
            assertEquals(userHandle.getUsername(), newUsername);
            resolve();
        });
        await();
    }
}

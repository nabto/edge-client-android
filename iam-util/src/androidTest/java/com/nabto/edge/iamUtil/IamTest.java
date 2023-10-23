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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
        System.gc();
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
    public void testPasswordOpenSuccessCallback() {
        connection = connectToDevice(localPairPasswordOpen);
        IamUtil iam = IamUtil.create();
        CompletableFuture future = new CompletableFuture();

        iam.pairPasswordOpenCallback(connection, uniqueUser(), localPairPasswordOpen.password, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            future.complete(null);
        });

        try {
            future.get();
        } catch (Exception e) {
            fail("Future.get() failed");
        }
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
    public void testPasswordOpenWrongPasswordCallback() {
        connection = connectToDevice(localPairPasswordOpen);
        IamUtil iam = IamUtil.create();
        CompletableFuture future = new CompletableFuture();

        iam.pairPasswordOpenCallback(connection, uniqueUser(), "i-am-a-clown-with-a-wrong-password", (ec, res) -> {
            assertEquals(ec, IamError.AUTHENTICATION_ERROR);
            future.complete(null);
        });

        try {
            future.get();
        } catch (Exception e) {
            fail("Future.get() failed");
        }
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
    public void testPasswordOpenBlockedByConfigCallback() {
        LocalDevice dev = localPasswordPairingDisabledConfig;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();
        CompletableFuture future = new CompletableFuture();

        iam.pairPasswordOpenCallback(connection, uniqueUser(), dev.password, (ec, res) -> {
            assertEquals(ec, IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            future.complete(null);
        });

        try {
            future.get();
        } catch (Exception e) {
            fail("Future.get() failed");
        }
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
    public void testLocalOpenSuccessCallback() {
        connection = connectToDevice(localPairLocalOpen);
        IamUtil iam = IamUtil.create();
        CompletableFuture future = new CompletableFuture();

        iam.isCurrentUserPairedCallback(connection, (ec0, res0) -> {
            assertEquals(ec0, IamError.NONE);
            assertFalse(res0.get());
            iam.pairLocalOpenCallback(connection, uniqueUser(), (ec1, res1) -> {
                assertEquals(ec1, IamError.NONE);
                iam.isCurrentUserPairedCallback(connection, (ec2, res2) -> {
                    assertEquals(ec2, IamError.NONE);
                    assertTrue(res2.get());
                    future.complete(null);
                });
            });
        });

        try {
            future.get();
        } catch (Exception e) {
            fail("Future.get() failed");
        }
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
    public void testLocalOpenInvalidUsernameCallback() {
        connection = connectToDevice(localPairLocalOpen);
        IamUtil iam = IamUtil.create();
        CompletableFuture future = new CompletableFuture();

        iam.pairLocalOpenCallback(connection, "Worst username in the history of usernames", (ec, res) -> {
            assertEquals(ec, IamError.INVALID_INPUT);
            future.complete(null);
        });

        try {
            future.get();
        } catch (Exception e) {
            fail("Future.get() failed");
        }
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
    public void testLocalOpenUsernameAlreadyExistsCallback() {
        LocalDevice dev = localPairLocalOpen;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();
        String username = uniqueUser();
        CompletableFuture future = new CompletableFuture();


        iam.pairLocalOpenCallback(connection, username, (_ec, _res) -> {
            iam.pairLocalOpenCallback(connection, username, (ec, res) -> {
                assertEquals(ec, IamError.USERNAME_EXISTS);
                future.complete(null);
            });
        });

        try {
            future.get();
        } catch (Exception e) {
            fail("Future.get() failed");
        }
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
    public void testLocalOpenBlockedByConfigCallback() {
        connection = connectToDevice(localPasswordPairingDisabledConfig);
        IamUtil iam = IamUtil.create();
        CompletableFuture future = new CompletableFuture();

        iam.pairLocalOpenCallback(connection, uniqueUser(), (ec, res) -> {
            assertEquals(ec, IamError.PAIRING_MODE_DISABLED);
            future.complete(null);
        });

        try {
            future.get();
        } catch (Exception e) {
            fail("Future.get() failed");
        }
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
    public void testPasswordInviteSuccessCallback() {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();
        
        String admin = uniqueUser();
        iam.pairPasswordOpenCallback(connection, admin, dev.password, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        iam.createUserCallback(connection, guest, guestPassword, "Guest", (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();
        iam.getUserCallback(connection, guest, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });

        // Reconnect as user instead of admin
        cleanup(connection);
        connection = connectToDevice(dev);
        iam.isCurrentUserPairedCallback(connection, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            assertFalse(res.get());
            resolve();
        });
        await();
        iam.pairPasswordInviteCallback(connection, guest, guestPassword, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();
        iam.isCurrentUserPairedCallback(connection, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            assertTrue(res.get());
            resolve();
        });
        await();
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
    public void testPasswordInviteWrongUserCallback() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        String admin = uniqueUser();
        String guest = uniqueUser();
        String guestPassword = "guestpassword";

        final AtomicInteger statusCode = new AtomicInteger();
        iam.pairPasswordOpenCallback(connection, admin, dev.password, (ec, res) -> {
            statusCode.set(ec.ordinal());
            resolve();
        });
        await();
        assertEquals(statusCode.get(), IamError.NONE.ordinal());

        statusCode.set(-1);
        iam.createUserCallback(connection, guest, guestPassword, "Guest", (ec, res) -> {
            statusCode.set(ec.ordinal());
            resolve();
        });
        await();
        assertEquals(statusCode.get(), IamError.NONE.ordinal());

        statusCode.set(-1);
        iam.getUserCallback(connection, guest, (ec, res) -> {
            statusCode.set(ec.ordinal());
            resolve();
        });
        await();
        assertEquals(statusCode.get(), IamError.NONE.ordinal());

        statusCode.set(-1);
        final AtomicBoolean result = new AtomicBoolean();
        // Reconnect as user instead of admin
        cleanup(connection);
        // Have to make a new variables to capture in lambda, java is really great...
        Connection connectionUser = connectToDevice(dev);
        iam.isCurrentUserPairedCallback(connectionUser, (ec, res) -> {
            statusCode.set(ec.ordinal());
            if (ec == IamError.NONE && res.isPresent()) {
                result.set(new Boolean(res.get()));
            }
            resolve();
        });
        await();
        assertEquals(IamError.NONE.ordinal(), statusCode.get());
        assertFalse(result.get());

        statusCode.set(-1);
        iam.pairPasswordInviteCallback(connectionUser, "bonk", guestPassword, (ec, res) -> {
            statusCode.set(ec.ordinal());
            resolve();
        });
        await();
        assertEquals(IamError.AUTHENTICATION_ERROR.ordinal(), statusCode.get());
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
    public void testCreateUserBadRoleCallback() {
        LocalDevice dev = localPasswordInvite;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        String admin = uniqueUser();
        iam.pairPasswordOpenCallback(connection, admin, dev.password, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        iam.createUserCallback(connection, guest, guestPassword, "Clown", (ec, res) -> {
            assertEquals(ec, IamError.ROLE_DOES_NOT_EXIST);
            resolve();
        });
        await();
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
    public void testCheckUnpairedUserCallback() {
        connection = connectToDevice(localPasswordInvite);
        IamUtil iam = IamUtil.create();
        CompletableFuture future = new CompletableFuture();

        iam.isCurrentUserPairedCallback(connection, (ec0, res0) -> {
            assertFalse(res0.get());
            iam.getCurrentUserCallback(connection, (ec1, res1) -> {
                assertEquals(ec1, IamError.USER_DOES_NOT_EXIST);
                future.complete(null);
            });
        });

        try {
            future.get();
        } catch (Exception e) {
            fail("Future.get() failed");
        }
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
    public void testCheckPairedUserNoIamSupportCallback() {
        connection = connectToDevice(localMdnsDevice);
        IamUtil iam = IamUtil.create();
        CompletableFuture future = new CompletableFuture();

        iam.isCurrentUserPairedCallback(connection, (ec, res) -> {
            assertEquals(ec, IamError.IAM_NOT_SUPPORTED);
            future.complete(null);
        });

        try {
            future.get();
        } catch (Exception e) {
            fail("Future.get() failed");
        }
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
    public void testCreateUserAndGetUserCallback() {
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
        iam.isCurrentUserPairedCallback(connectionUser, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            assertFalse(res.get());
            resolve();
        });
        await();
        iam.pairPasswordInviteCallback(connectionUser, guest, guestPassword, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();
        iam.isCurrentUserPairedCallback(connectionUser, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            assertTrue(res.get());
            resolve();
        });
        await();

        // Guest cannot GET admin
        iam.getUserCallback(connectionUser, admin, (ec, res) -> {
            assertEquals(ec, IamError.BLOCKED_BY_DEVICE_CONFIGURATION);
            resolve();
        });
        await();

        // Guest can GET self
        iam.getUserCallback(connectionUser, guest, (ec, opt) -> {
            assertEquals(ec, IamError.NONE);
            IamUser me = opt.get();
            assertEquals(me.getUsername(), guest);
            assertEquals(me.getRole(), "Guest");
            resolve();
        });
        await();

        cleanup(connectionUser);
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
    public void testSetDisplayNameCallback() {
        LocalDevice dev = localPairLocalOpen;
        connection = connectToDevice(dev);
        IamUtil iam = IamUtil.create();

        String username = uniqueUser();
        String displayName = uniqueUser();

        iam.pairLocalOpen(connection, username);
        iam.updateUserDisplayNameCallback(connection, username, displayName, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();

        iam.getCurrentUserCallback(connection, (ec, opt) -> {
            assertEquals(ec, IamError.NONE);
            IamUser user = opt.get();
            assertEquals(user.username, username);
            assertEquals(user.displayName, displayName);
            resolve();
        });
        await();
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
    public void testDeleteUserCallback() {
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

        iam.deleteUserCallback(connection, "nonexistent", (ec, res) -> {
            assertEquals(ec, IamError.USER_DOES_NOT_EXIST);
            resolve();
        });
        await();

        iam.deleteUserCallback(connection, guest, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();

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
    public void testLocalInitialSuccessCallback() {
        connection = connectToDeviceWithAdminKey(localPairLocalInitial);
        IamUtil iam = IamUtil.create();
        CompletableFuture future = new CompletableFuture();

        iam.isCurrentUserPairedCallback(connection, (ec0, res0) -> {
            assertEquals(ec0, IamError.NONE);
            assertFalse(res0.get());
            iam.pairLocalInitialCallback(connection, (ec1, res1) -> {
                assertEquals(ec1, IamError.NONE);
                iam.isCurrentUserPairedCallback(connection, (ec2, res2) -> {
                    assertEquals(ec2, IamError.NONE);
                    assertTrue(res2.get());
                    future.complete(null);
                });
            });
        });

        try {
            future.get();
        } catch (Exception e) {
            fail("Future.get() failed");
        }
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
    public void testLocalInitialAlreadyPairedFailCallback() {
        connection = connectToDeviceWithAdminKey(localPairLocalInitial);
        IamUtil iam = IamUtil.create();
        iam.isCurrentUserPairedCallback(connection, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            assertFalse(res.get());
            resolve();
        });
        await();

        iam.pairLocalInitialCallback(connection, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();

        iam.isCurrentUserPairedCallback(connection, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            assertTrue(res.get());
            resolve();
        });
        await();

        iam.pairLocalInitialCallback(connection, (ec, res) -> {
            assertEquals(ec, IamError.INITIAL_USER_ALREADY_PAIRED);
            resolve();
        });
        await();
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
    public void testUpdateUserPasswordCallback() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        String guestPassword = "guestpassword";
        IamUtil iam = IamUtil.create();

        String user = createAdminAndGuest(connection, dev, guestPassword);
        String newGuestPassword = "newguestpassword";

        // Should fail
        iam.updateUserPasswordCallback(connection, "baduser", "sus", (ec, res) -> {
            assertEquals(ec, IamError.USER_DOES_NOT_EXIST);
            resolve();
        });
        await();

        iam.updateUserPasswordCallback(connection, user, newGuestPassword, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();

        // Reconnect as user instead of admin
        cleanup(connection);
        Connection connectionUser = connectToDevice(dev);
        iam.isCurrentUserPairedCallback(connectionUser, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            assertFalse(res.get());
            resolve();
        });
        await();

        iam.pairPasswordInviteCallback(connectionUser, user, guestPassword, (ec, res) -> {
            assertEquals(ec, IamError.AUTHENTICATION_ERROR);
            resolve();
        });
        await();

        iam.pairPasswordInviteCallback(connectionUser, user, newGuestPassword, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            resolve();
        });
        await();

        iam.isCurrentUserPairedCallback(connectionUser, (ec, res) -> {
            assertEquals(ec, IamError.NONE);
            assertTrue(res.get());
            resolve();
        });
        await();

        cleanup(connectionUser);
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

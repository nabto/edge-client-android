package com.nabto.edge.iamutil;

import com.nabto.edge.iamutil.Iam.PairingMode;
import com.nabto.edge.client.NabtoClient;
import com.nabto.edge.client.Connection;

import java.util.List;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;

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
 */


@RunWith(AndroidJUnit4.class)
public class IamTest {
    private NabtoClient client;

    private String localInitialAdminKey =
        "-----BEGIN EC PRIVATE KEY-----\n" +
        "MHcCAQEEIAl3ZURem5NMCTZA0OeTPcT7y6T2FHjHhmQz54UiH7mQoAoGCCqGSM49\n" +
        "AwEHoUQDQgAEbiabrII+WZ8ABD4VQpmLe3cSIWdQfrRbxXotx5yxwInfgLuDU+rq\n" +
        "OIFReqTf5h+Nwp/jj00fnsII88n1YCveoQ==\n" +
        "-----END EC PRIVATE KEY-----\n";

    @Before
    public void setup() {
        client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
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
        assertEquals(IamException.class, e.getClass());
        IamException iamEx = (IamException)e;
        assertEquals(iamEx.getError(), error);
    }

    private Connection connectToDevice(LocalDevice dev) {
        Connection connection = client.createConnection();
        String pk = client.createPrivateKey();
        connection.updateOptions(dev.json(pk));
        connection.connect();
        return connection;
    }

    private Connection connectToDeviceWithAdminKey(LocalDevice dev) {
        Connection connection = client.createConnection();
        connection.updateOptions(dev.json(localInitialAdminKey));
        connection.connect();

        Iam iam = Iam.create();
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
        Connection connection = connectToDevice(localPairPasswordOpen);
        Iam iam = Iam.create();
        iam.pairPasswordOpen(connection, uniqueUser(), localPairPasswordOpen.password);
    }

    @Test
    public void testPasswordOpenWrongPassword() {
        Connection connection = connectToDevice(localPairPasswordOpen);
        Iam iam = Iam.create();
        assertIamError(IamError.AUTHENTICATION_ERROR, () -> {
            iam.pairPasswordOpen(connection, uniqueUser(), "i-am-a-clown-with-a-wrong-password");
        });
    }

    @Test
    public void testPasswordOpenBlockedByConfig() {
        LocalDevice dev = localPasswordPairingDisabledConfig;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create();
        assertIamError(IamError.BLOCKED_BY_DEVICE_CONFIGURATION, () -> {
            iam.pairPasswordOpen(connection, uniqueUser(), dev.password);
        });
    }

    @Test
    public void testLocalOpenSuccess() {
        Connection connection = connectToDevice(localPairLocalOpen);
        Iam iam = Iam.create();

        assertFalse(iam.isCurrentUserPaired(connection));
        iam.pairLocalOpen(connection, uniqueUser());
        assertTrue(iam.isCurrentUserPaired(connection));
    }

    @Test
    public void testLocalOpenCallbackSuccess() {
        Connection connection = connectToDevice(localPairLocalOpen);
        Iam iam = Iam.create();

        CompletableFuture<Boolean> future1 = new CompletableFuture<>();
        iam.isCurrentUserPairedCallback(connection, (ec, arg) -> {
            future1.complete(arg.get());
        });

        try {
            boolean result = future1.get();
            assertFalse(result);
        } catch (Exception e) {
            fail("Asynchronous test failed with exception " + e.toString());
        }

        iam.pairLocalOpen(connection, uniqueUser());

        CompletableFuture<Boolean> future2 = new CompletableFuture<>();
        iam.isCurrentUserPairedCallback(connection, (ec, arg) -> {
            future2.complete(arg.get());
        });

        try {
            boolean result = future2.get();
            assertTrue(result);
        } catch (Exception e) {
            fail("Asynchronous test failed with exception " + e.toString());
        }
    }

    @Test
    public void testLocalOpenInvalidUsername() {
        Connection connection = connectToDevice(localPairLocalOpen);
        Iam iam = Iam.create();
        assertIamError(IamError.INVALID_INPUT, () -> {
            iam.pairLocalOpen(connection, "Worst username in the history of usernames");
        });
    }

    @Test
    public void testLocalOpenUsernameAlreadyExists() {
        LocalDevice dev = localPairLocalOpen;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create();
        String username = uniqueUser();
        iam.pairLocalOpen(connection, username);
        assertIamError(IamError.USERNAME_EXISTS, () -> {
            iam.pairLocalOpen(connection, username);
        });
    }

    @Test
    public void testLocalOpenBlockedByConfig() {
        Connection connection = connectToDevice(localPasswordPairingDisabledConfig);
        Iam iam = Iam.create();
        assertIamError(IamError.PAIRING_MODE_DISABLED, () -> {
            iam.pairLocalOpen(connection, uniqueUser());
        });
    }

    @Test
    public void testPasswordInviteSuccess() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create();

        String admin = uniqueUser();
        iam.pairPasswordOpen(connection, admin, dev.password);

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        System.out.println(guest);
        iam.createUser(connection, guest, guestPassword, "Guest");
        iam.getUser(connection, guest);

        // Reconnect as user instead of admin
        connection.close();
        connection = connectToDevice(dev);
        iam = Iam.create();
        assertFalse(iam.isCurrentUserPaired(connection));
        iam.pairPasswordInvite(connection, guest, guestPassword);
        assertTrue(iam.isCurrentUserPaired(connection));
    }

    @Test
    public void testPasswordInviteWrongUser() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create();

        String admin = uniqueUser();
        iam.pairPasswordOpen(connection, admin, dev.password);

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        iam.createUser(connection, guest, guestPassword, "Guest");
        iam.getUser(connection, guest);

        // Reconnect as user instead of admin
        connection.close();
        // Have to make a new variables to capture in lambda, java is really great...
        Connection connectionUser = connectToDevice(dev);
        assertFalse(iam.isCurrentUserPaired(connectionUser));
        assertIamError(IamError.AUTHENTICATION_ERROR, () -> {
            iam.pairPasswordInvite(connectionUser, "bonk", guestPassword);
        });
    }

    @Test
    public void testCreateUserBadRole() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create();

        String admin = uniqueUser();
        iam.pairPasswordOpen(connection, admin, dev.password);

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        assertIamError(IamError.ROLE_DOES_NOT_EXIST, () -> {
            iam.createUser(connection, guest, guestPassword, "Clown");
        });
    }

    @Test
    public void testCheckUnpairedUser() {
        Connection connection = connectToDevice(localPasswordInvite);
        Iam iam = Iam.create();
        assertFalse(iam.isCurrentUserPaired(connection));
        assertIamError(IamError.USER_DOES_NOT_EXIST, () -> {
            iam.getCurrentUser(connection);
        });
    }

    @Test
    public void testCheckPairedUserNoIamSupport() {
        Connection connection = connectToDevice(localMdnsDevice);
        Iam iam = Iam.create();
        assertIamError(IamError.IAM_NOT_SUPPORTED, () -> {
            iam.isCurrentUserPaired(connection);
        });
    }

    @Test
    public void testCreateUserAndGetUser() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create();
        String admin = uniqueUser();
        iam.pairPasswordOpen(connection, admin, dev.password);

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        iam.createUser(connection, guest, guestPassword, "Guest");

        // Reconnect as user instead of admin
        connection.close();
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
    }

    @Test
    public void testSetDisplayName() {
        LocalDevice dev = localPairLocalOpen;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create();

        String username = uniqueUser();
        String displayName = uniqueUser();

        iam.pairLocalOpen(connection, username);
        iam.updateUserDisplayName(connection, username, displayName);

        IamUser user = iam.getCurrentUser(connection);
        assertEquals(user.username, username);
        assertEquals(user.displayName, displayName);
    }

    @Test
    public void testDeleteUser() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create();

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
    public void testLocalInitialSuccess() {
        Connection connection = connectToDeviceWithAdminKey(localPairLocalInitial);
        Iam iam = Iam.create();
        assertFalse(iam.isCurrentUserPaired(connection));
        iam.pairLocalInitial(connection);
        assertTrue(iam.isCurrentUserPaired(connection));
    }

    @Test
    public void testLocalInitialAlreadyPairedFail() {
        Connection connection = connectToDeviceWithAdminKey(localPairLocalInitial);
        Iam iam = Iam.create();
        assertFalse(iam.isCurrentUserPaired(connection));
        iam.pairLocalInitial(connection);
        assertTrue(iam.isCurrentUserPaired(connection));
        assertIamError(IamError.INITIAL_USER_ALREADY_PAIRED, () -> {
            iam.pairLocalInitial(connection);
        });
    }

    @Test
    public void testGetDeviceDetails() {
        LocalDevice dev = localPairLocalInitial;
        Connection connection = connectToDeviceWithAdminKey(dev);
        Iam iam = Iam.create();
        IamDeviceDetails details = iam.getDeviceDetails(connection);
        assertEquals(details.getProductId(), dev.productId);
        assertEquals(details.getDeviceId(), dev.deviceId);
        assertArrayEquals(details.getModes(), new String[] { "LocalInitial" });
    }

    @Test
    public void testGetDeviceDetailsCallback() {
        LocalDevice dev = localPairLocalInitial;
        Connection connection = connectToDeviceWithAdminKey(dev);
        Iam iam = Iam.create();

        CompletableFuture<IamDeviceDetails> future = new CompletableFuture<>();
        iam.getDeviceDetailsCallback(connection, (error, value) -> {
            assertEquals(error, IamError.NONE);
            future.complete(value.get());
        });

        try {
            IamDeviceDetails details = future.get();
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
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create();

        PairingMode[] modes = iam.getAvailablePairingModes(connection);
        assertEquals(modes.length, 2);

        List<PairingMode> modesList = Arrays.asList(modes);
        assertTrue(modesList.contains(PairingMode.PASSWORD_INVITE));
        assertTrue(modesList.contains(PairingMode.PASSWORD_OPEN));
    }

    @Test
    public void testGetPairingModes2() {
        LocalDevice dev = localPairLocalInitial;
        Connection connection = connectToDeviceWithAdminKey(dev);
        Iam iam = Iam.create();

        PairingMode[] modes = iam.getAvailablePairingModes(connection);
        assertEquals(modes.length, 1);

        List<PairingMode> modesList = Arrays.asList(modes);
        assertTrue(modesList.contains(PairingMode.LOCAL_INITIAL));
    }

    @Test
    public void testGetPairingModes3() {
        LocalDevice dev = localPairLocalOpen;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create();

        PairingMode[] modes = iam.getAvailablePairingModes(connection);
        assertEquals(modes.length, 1);

        List<PairingMode> modesList = Arrays.asList(modes);
        assertTrue(modesList.contains(PairingMode.LOCAL_OPEN));
    }

    private String createAdminAndGuest(Connection connection, LocalDevice dev, String guestPassword) {
        String admin = uniqueUser();
        Iam iam = Iam.create();
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
        Iam iam = Iam.create();

        String user = createAdminAndGuest(connection, dev, guestPassword);
        String newGuestPassword = "newguestpassword";

        // Should fail
        assertIamError(IamError.USER_DOES_NOT_EXIST, () -> {
            iam.updateUserPassword(connection, "baduser", "sus");
        });

        // Shouldn't fail
        iam.updateUserPassword(connection, user, newGuestPassword);

        // Reconnect as user instead of admin
        connection.close();
        Connection connectionUser = connectToDevice(dev);
        assertFalse(iam.isCurrentUserPaired(connectionUser));

        assertIamError(IamError.AUTHENTICATION_ERROR, () -> {
            iam.pairPasswordInvite(connectionUser, user, guestPassword);
        });
        iam.pairPasswordInvite(connectionUser, user, newGuestPassword);
        assertTrue(iam.isCurrentUserPaired(connectionUser));
    }

    @Test
    public void testUpdateUserRole() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        String guestPassword = "guestpassword";
        Iam iam = Iam.create();

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
    public void testUpdateUserDisplayName() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        String guestPassword = "guestpassword";
        Iam iam = Iam.create();

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
    public void testRenameUser() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        String guestPassword = "guestpassword";
        Iam iam = Iam.create();

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
}

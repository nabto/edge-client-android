package com.nabto.edge.client;

import java.util.UUID;
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
 * Then run the tests in a separate terminal
 *   ./gradlew library:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.nabto.edge.client.IamTest
 */


@RunWith(AndroidJUnit4.class)
public class IamTest {
    private NabtoClient client;

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
        NabtoRuntimeException e = assertThrows(NabtoRuntimeException.class, () -> {
            iam.pairPasswordOpen(connection, uniqueUser(), "i-am-a-clown-with-a-wrong-password");
        });
        assertEquals(e.getErrorCode().getErrorCode(), ErrorCodes.UNAUTHORIZED);
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
        Iam iamUser = Iam.create();
        assertFalse(iamUser.isCurrentUserPaired(connectionUser));
        NabtoRuntimeException e = assertThrows(NabtoRuntimeException.class, () -> {
            iamUser.pairPasswordInvite(connectionUser, "bonk", guestPassword);
        });
        assertEquals(e.getErrorCode().getErrorCode(), ErrorCodes.UNAUTHORIZED);
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
        Iam iamUser = Iam.create();
        assertFalse(iamUser.isCurrentUserPaired(connectionUser));
        iamUser.pairPasswordInvite(connectionUser, guest, guestPassword);
        assertTrue(iamUser.isCurrentUserPaired(connectionUser));

        // Guest is not allowed to GET admin
        assertIamError(IamError.BLOCKED_BY_DEVICE_CONFIGURATION, () -> {
            iamUser.getUser(connectionUser, admin);
        });

        // Guest can GET self
        IamUser me = iamUser.getUser(connectionUser, guest);
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

    }

    @Test
    public void testCodableUser() {

    }

    @Test
    public void testLocalInitialSuccess() {

    }

    @Test
    public void testLocalInitialAlreadyPairedFail() {

    }

    @Test
    public void testGetDeviceDetails() {

    }

    @Test
    public void testGetPairingModes1() {

    }

    @Test
    public void testGetPairingModes2() {

    }

    @Test
    public void testGetPairingModes3() {

    }
}

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
        Iam iam = Iam.create(connection);
        iam.pairPasswordOpen(uniqueUser(), localPairPasswordOpen.password);
    }

    @Test
    public void testPasswordOpenWrongPassword() {
        Connection connection = connectToDevice(localPairPasswordOpen);
        Iam iam = Iam.create(connection);
        NabtoRuntimeException e = assertThrows(NabtoRuntimeException.class, () -> {
            iam.pairPasswordOpen(uniqueUser(), "i-am-a-clown-with-a-wrong-password");
        });
        assertEquals(e.getErrorCode().getErrorCode(), ErrorCodes.UNAUTHORIZED);
    }

    @Test
    public void testPasswordOpenBlockedByConfig() {
        LocalDevice dev = localPasswordPairingDisabledConfig;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create(connection);
        assertIamError(IamError.BLOCKED_BY_DEVICE_CONFIGURATION, () -> {
            iam.pairPasswordOpen(uniqueUser(), dev.password);
        });
    }

    @Test
    public void testLocalOpenSuccess() {
        Connection connection = connectToDevice(localPairLocalOpen);
        Iam iam = Iam.create(connection);

        assertFalse(iam.isCurrentUserPaired());
        iam.pairLocalOpen(uniqueUser());
        assertTrue(iam.isCurrentUserPaired());
    }

    @Test
    public void testLocalOpenInvalidUsername() {
        Connection connection = connectToDevice(localPairLocalOpen);
        Iam iam = Iam.create(connection);
        assertIamError(IamError.INVALID_INPUT, () -> {
            iam.pairLocalOpen("Worst username in the history of usernames");
        });
    }

    @Test
    public void testLocalOpenUsernameAlreadyExists() {
        LocalDevice dev = localPairLocalOpen;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create(connection);
        String username = uniqueUser();
        iam.pairLocalOpen(username);
        assertIamError(IamError.USERNAME_EXISTS, () -> {
            iam.pairLocalOpen(username);
        });
    }

    @Test
    public void testLocalOpenBlockedByConfig() {
        Connection connection = connectToDevice(localPasswordPairingDisabledConfig);
        Iam iam = Iam.create(connection);
        assertIamError(IamError.PAIRING_MODE_DISABLED, () -> {
            iam.pairLocalOpen(uniqueUser());
        });
    }

    @Test
    public void testPasswordInviteSuccess() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create(connection);

        String admin = uniqueUser();
        iam.pairPasswordOpen(admin, dev.password);
        
        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        System.out.println(guest);
        iam.createUser(guest, guestPassword, "Guest");
        iam.getUser(guest);

        // Reconnect as user instead of admin
        connection.close();
        connection = connectToDevice(dev);
        iam = Iam.create(connection);
        assertFalse(iam.isCurrentUserPaired());
        iam.pairPasswordInvite(guest, guestPassword);
        assertTrue(iam.isCurrentUserPaired());
    }

    @Test
    public void testPasswordInviteWrongUser() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create(connection);

        String admin = uniqueUser();
        iam.pairPasswordOpen(admin, dev.password);

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        System.out.println(guest);
        iam.createUser(guest, guestPassword, "Guest");
        iam.getUser(guest);

        // Reconnect as user instead of admin
        connection.close();
        connection = connectToDevice(dev);
        // Have to make a new iam variable to capture in lambda, java is really great...
        Iam iam2 = Iam.create(connection);
        assertFalse(iam2.isCurrentUserPaired());
        NabtoRuntimeException e = assertThrows(NabtoRuntimeException.class, () -> {
            iam2.pairPasswordInvite("bonk", guestPassword);
        });
        assertEquals(e.getErrorCode().getErrorCode(), ErrorCodes.UNAUTHORIZED);
    }

    @Test
    public void testCreateUserBadRole() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create(connection);

        String admin = uniqueUser();
        iam.pairPasswordOpen(admin, dev.password);

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        System.out.println(guest);
        assertIamError(IamError.ROLE_DOES_NOT_EXIST, () -> {
            iam.createUser(guest, guestPassword, "Clown"); 
        });
    }

    @Test
    public void testCheckUnpairedUser() {
        Connection connection = connectToDevice(localPasswordInvite);
        Iam iam = Iam.create(connection);
        assertFalse(iam.isCurrentUserPaired());
        assertIamError(IamError.USER_DOES_NOT_EXIST, () -> {
            iam.getCurrentUser();
        });
    }

    @Test
    public void testCheckPairedUserNoIamSupport() {

    }

    @Test
    public void testCreateUserAndGetUser() {
        LocalDevice dev = localPasswordInvite;
        Connection connection = connectToDevice(dev);
        Iam iam = Iam.create(connection);
        String admin = uniqueUser();
        iam.pairPasswordOpen(admin, dev.password);

        String guest = uniqueUser();
        String guestPassword = "guestpassword";
        iam.createUser(guest, guestPassword, "Guest");

        // Reconnect as user instead of admin
        connection.close();
        connection = connectToDevice(dev);
        Iam iamUser = Iam.create(connection);
        assertFalse(iamUser.isCurrentUserPaired());
        iamUser.pairPasswordInvite(guest, guestPassword);
        assertTrue(iamUser.isCurrentUserPaired());

        // Guest is not allowed to GET admin
        assertIamError(IamError.BLOCKED_BY_DEVICE_CONFIGURATION, () -> {
            iamUser.getUser(admin);
        });

        // Guest can GET self
        IamUser me = iamUser.getUser(guest);
        assertNotNull(me);
        assertEquals(me.getUsername(), guest);
        assertEquals(me.getRole(), "Guest");
    }

    
}

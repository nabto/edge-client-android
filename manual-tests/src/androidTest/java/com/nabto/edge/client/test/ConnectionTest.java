package com.nabto.edge.client.test;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.Optional;

import com.nabto.edge.client.NabtoClient;
import com.nabto.edge.client.Connection;
import com.nabto.edge.client.NabtoNoChannelsException;
import com.nabto.edge.client.ErrorCodes;


/**
 * Instrumented test, which will execute on an Android device.
 *
 */
@RunWith(AndroidJUnit4.class)
public class ConnectionTest {

    // note: test will fail in emulator, at least on macOS (local test devices not discoverable from within emulator's default network)
    @Test(expected = Test.None.class)
    public void connectLocal() throws Exception {
//        final Context context = InstrumentationRegistry.getInstrumentation().getContext();
//        final ConnectivityManager connectivityManager = (ConnectivityManager)(context.getSystemService(Context.CONNECTIVITY_SERVICE));
//        for (Network network : connectivityManager.getAllNetworks()) {
//            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
//            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    connectivityManager.bindProcessToNetwork(network);
//                    Log.d("nabto", "bindProcessToNetwork(some_wifi) ok");
//                } else {
//                    // For older Android versions, use the deprecated method
//                    ConnectivityManager.setProcessDefaultNetwork(network);
//                    Log.d("nabto", "setProcessDefaultNetwork(wome_wifi) ok");
//                }
//            }
//        }

        try (NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext())) {
            client.setLogLevel("trace");
            try (Connection connection = Helper.createLocalConnection(client)) {
                connection.connect();
                connection.connectionClose();
            } catch (Exception e) {
                if (e instanceof NabtoNoChannelsException) {
                    fail("NabtoNoChannelsException - local error: " + ((NabtoNoChannelsException) e).getLocalChannelErrorCode().getDescription() +
                            "; remote error: " + ((NabtoNoChannelsException) e).getRemoteChannelErrorCode().getDescription());
                } else {
                    throw e;
                }
            }
        }
    }


   @Test(expected = Test.None.class)
    public void noSuchLocalDevice() throws Exception {
        NabtoClient client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        Connection connection = Helper.createConnection(client);
        JSONObject options = new JSONObject();
        options.put("Local", true);
        options.put("Remote", false);
        options.put("DeviceId", "unknown");
        connection.updateOptions(options.toString());

        try {
            connection.connect();
            fail();

        } catch (NabtoNoChannelsException e) {
            assert(e.getLocalChannelErrorCode().getErrorCode() == ErrorCodes.NOT_FOUND);
        }
    }
};

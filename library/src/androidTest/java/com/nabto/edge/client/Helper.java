package com.nabto.edge.client;

import android.content.res.Resources;

import androidx.test.platform.app.InstrumentationRegistry;

import com.nabto.edge.client.testdata.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Helper {

    public static Connection createConnection(NabtoClient client) {
        Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();

        Connection connection = client.createConnection();
        JSONObject options = new JSONObject();
        try {
            options.put("ProductId", resources.getString(R.string.product_id));
            options.put("DeviceId", resources.getString(R.string.device_id));
            options.put("ServerKey", resources.getString(R.string.server_key));
            options.put("PrivateKey", client.createPrivateKey());
            connection.updateOptions(options.toString());
        } catch (JSONException e) {
            return null;
        }
        return connection;
    }

    public static Connection createRemoteConnection(NabtoClient client) {
        Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();

        Connection connection = client.createConnection();
        JSONObject options = new JSONObject();
        try {
            options.put("ServerConnectToken", "demosct");
            options.put("ProductId", resources.getString(R.string.product_id));
            options.put("DeviceId", resources.getString(R.string.device_id));
            options.put("ServerKey", resources.getString(R.string.server_key));
            options.put("PrivateKey", client.createPrivateKey());
            options.put("Local", false);
            options.put("Remote", true);
            connection.updateOptions(options.toString());
        } catch (JSONException e) {
            return null;
        }
        return connection;
    }

    public static Connection createLocalConnection(NabtoClient client) {
        Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();

        Connection connection = client.createConnection();
        JSONObject options = new JSONObject();
        try {
            options.put("ProductId", resources.getString(R.string.local_product_id));
            options.put("DeviceId", resources.getString(R.string.local_device_id));
            options.put("PrivateKey", client.createPrivateKey());
            options.put("Local", true);
            options.put("Remote", false);
            connection.updateOptions(options.toString());
        } catch (JSONException e) {
            return null;
        }
        return connection;
    }


    public static Connection createTunnelConnection(NabtoClient client) {
        Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();

        Connection connection = client.createConnection();
        JSONObject options = new JSONObject();
        try {
            options.put("ProductId", resources.getString(R.string.tunnel_product_id));
            options.put("DeviceId", resources.getString(R.string.tunnel_device_id));
            options.put("ServerKey", resources.getString(R.string.tunnel_server_key));
            options.put("PrivateKey", client.createPrivateKey());
            options.put("ServerConnectToken", resources.getString(R.string.tunnel_sct));
            connection.updateOptions(options.toString());
        } catch (JSONException e) {
            return null;
        }
        return connection;
    }

    public static Connection createStreamConnection(NabtoClient client) {
        Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();

        Connection connection = client.createConnection();
        JSONObject options = new JSONObject();
        try {
            options.put("ProductId", resources.getString(R.string.stream_product_id));
            options.put("DeviceId", resources.getString(R.string.stream_device_id));
            options.put("ServerConnectToken", "demosct");
            options.put("PrivateKey", client.createPrivateKey());
            connection.updateOptions(options.toString());
        } catch (JSONException e) {
            return null;
        }
        return connection;
    }

}

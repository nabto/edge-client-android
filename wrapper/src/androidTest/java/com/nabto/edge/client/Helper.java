package com.nabto.edge.client;


//import android.content.Context;
import android.content.res.Resources;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;

import static org.junit.Assert.*;

import com.nabto.edge.client.test.R;


public class Helper {

    public static Connection createConnection(NabtoClient client) throws NabtoException {
        Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();
        Connection connection = client.createConnection();
        JSONObject options = new JSONObject();
        try {
            options.put("ProductId", resources.getString(R.string.product_id));
            options.put("DeviceId", resources.getString(R.string.device_id));
            options.put("ServerKey", resources.getString(R.string.server_key));
            options.put("ServerUrl", resources.getString(R.string.server_url));
            options.put("PrivateKey", client.createPrivateKey());
        } catch (JSONException e) {

        }
        connection.setOptions(options.toString());
        return connection;
    }

}

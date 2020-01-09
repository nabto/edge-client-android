package com.nabto.client.jni;


//import android.content.Context;
import android.content.res.Resources;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;

import static org.junit.Assert.*;

import com.nabto.client.jni.test.R;

public class Helper {

    public static Connection createConnection(Context context) throws NabtoException {
        Resources resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();
        LoggerImpl logger = new LoggerImpl();
        context.setLogger(logger);
        context.setLogLevel("trace");
        Connection connection = context.createConnection();
        connection.setProductId(resources.getString(R.string.product_id));
        connection.setDeviceId(resources.getString(R.string.device_id));
        connection.setServerKey(resources.getString(R.string.server_key));
        connection.setServerUrl(resources.getString(R.string.server_url));
        connection.setPrivateKey(context.createPrivateKey());
        return connection;
    }

}

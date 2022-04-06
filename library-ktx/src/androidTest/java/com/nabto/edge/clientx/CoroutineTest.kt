package com.nabto.edge.clientx;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert.*;
import androidx.test.core.app.launchActivity

import android.app.Activity;
import android.content.res.Resources;
import androidx.lifecycle.Lifecycle.State;
import androidx.lifecycle.lifecycleScope;
import kotlinx.coroutines.*
import org.json.JSONObject

import com.nabto.edge.client.Connection
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.clientx.*

class TestActivity : Activity() {
    public fun runTest() {
        var connection : Connection? = null

        // @TODO: Using a variable like this to check that finally block was reached is maybe not the best option? needs more research.
        var wasClosed = false

        // @TODO: We want to use this lifecycleScope but its not working.
        // this.lifecycleScope.launch {
        runBlocking {
            try {
                val client : NabtoClient = NabtoClient.create(this@TestActivity);
                connection = client.createConnection();
                val options = JSONObject()

                options.put("ProductId", resources.getString(com.nabto.edge.clientx.test.R.string.stream_product_id));
                options.put("DeviceId", resources.getString(com.nabto.edge.clientx.test.R.string.stream_device_id));
                options.put("ServerKey", resources.getString(com.nabto.edge.clientx.test.R.string.stream_server_key));
                options.put("PrivateKey", client.createPrivateKey())

                connection?.updateOptions(options.toString())
                connection?.connectAsync()
            } finally {
                // Assert that there is a connection that is then closed.
                assertNotNull(connection)
                connection?.close()
                wasClosed = true
            }
        }

        assertTrue(wasClosed)
    }
}

@RunWith(AndroidJUnit4::class)
class CoroutineTest {
    @Test
    fun testCoroutineCancellation() {
        val scenario = launchActivity<TestActivity>()
        assertEquals(scenario.getState(), State.RESUMED)
        scenario.onActivity { activity ->
            activity.runTest()
        }
        scenario.close()
    }
}

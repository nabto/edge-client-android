package com.nabto.edge.client.ktx
import com.nabto.edge.client.ktx.test.R

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.json.JSONObject

import com.nabto.edge.client.Connection
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.client.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

suspend fun createStreamConnection(client : NabtoClient): Connection? {
    val resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();
    val connection = client.createConnection()
    val options = JSONObject()

    options.put("ProductId", resources.getString(R.string.stream_product_id))
    options.put("DeviceId", resources.getString(R.string.stream_device_id))
    options.put("PrivateKey", client.createPrivateKey())
    options.put("ServerConnectToken", resources.getString(R.string.stream_server_connect_token))

    connection?.updateOptions(options.toString())
    connection?.awaitConnect()
    return connection
}

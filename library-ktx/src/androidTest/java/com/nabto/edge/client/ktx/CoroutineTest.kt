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

suspend fun createConnection(client : NabtoClient): Connection? {
    val resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();
    val connection = client.createConnection()
    val options = JSONObject()

    options.put("ProductId", resources.getString(R.string.stream_product_id))
    options.put("DeviceId", resources.getString(R.string.stream_device_id))
    options.put("ServerKey", resources.getString(R.string.stream_server_key))
    options.put("PrivateKey", client.createPrivateKey())

    connection?.updateOptions(options.toString())
    connection?.awaitConnect()
    return connection
}

@RunWith(AndroidJUnit4::class)
class CoroutineTest {
    val testScheduler = TestCoroutineScheduler()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val testScope = TestScope(testDispatcher)

    @Test
    fun testCoroutineCancellation() = testScope.runTest {
        var connection: Connection? = null
        var wasCancelled = false
        val latch = CountDownLatch(1)

        val job = launch {
            try {
                val client: NabtoClient = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
                connection = createConnection(client)
                awaitCancellation()
            } finally {
                wasCancelled = true
                connection = null
                latch.countDown()
            }
        }

        // runCurrent runs the pending job above which will then suspend when it getes to awaitCancellation 
        testScheduler.runCurrent()
        job.cancelAndJoin()

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(wasCancelled)
        assertNull(connection)
    }
}

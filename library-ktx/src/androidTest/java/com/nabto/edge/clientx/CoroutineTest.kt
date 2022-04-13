package com.nabto.edge.clientx
import com.nabto.edge.clientx.test.R

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import kotlinx.coroutines.*
import org.json.JSONObject

import com.nabto.edge.client.Connection
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.clientx.*

suspend fun createConnection(client : NabtoClient): Connection? {
    val resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();
    val connection = client.createConnection()
    val options = JSONObject()

    options.put("ProductId", resources.getString(R.string.stream_product_id))
    options.put("DeviceId", resources.getString(R.string.stream_device_id))
    options.put("ServerKey", resources.getString(R.string.stream_server_key))
    options.put("PrivateKey", client.createPrivateKey())

    connection?.updateOptions(options.toString())
    connection?.connectAsync()
    return connection
}

@RunWith(AndroidJUnit4::class)
class CoroutineTest {
    @Test
    fun test() {
        val mockScope = MainScope()
        var connection : Connection? = null
        var wasCancelled = false
        var wasClosed = false

        val job = mockScope.launch {
            try {
                val client : NabtoClient = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
                connection = createConnection(client)
                assertNotNull(connection)
            } catch(e: CancellationException) {
                wasCancelled = true
            } finally {
                wasClosed = true
                // @TODO: This is a blocking call, is there any issues with that?
                connection?.close()
                connection = null
            }
        }

        // This throws a CancellationException into the coroutine children
        // An android lifecycleScope will call .cancel() when the lifecycle is over
        // Hopefully this mock test is close enough to reality
        mockScope.cancel()
        runBlocking {
            // Wait for the job to finish everything
            job.cancelAndJoin()
        }

        assertTrue(wasCancelled)
        assertTrue(wasClosed)
        assertNull(connection)
    }
}

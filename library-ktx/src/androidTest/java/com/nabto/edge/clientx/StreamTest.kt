package com.nabto.edge.clientx
import com.nabto.edge.clientx.test.R

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import kotlinx.coroutines.*

import com.nabto.edge.client.NabtoClient
import com.nabto.edge.client.Connection
import com.nabto.edge.client.NabtoEOFException
import com.nabto.edge.clientx.*
import org.json.JSONException
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class StreamTest {
    suspend fun createStreamConnection(client: NabtoClient): Connection? {
        val resources = InstrumentationRegistry.getInstrumentation().getContext().getResources();

        val connection = client.createConnection();
        val options = JSONObject();
        try {
            options.put("ProductId", resources.getString(R.string.stream_product_id));
            options.put("DeviceId", resources.getString(R.string.stream_device_id));
            options.put("ServerKey", resources.getString(R.string.stream_server_key));
            options.put("PrivateKey", client.createPrivateKey());
            connection.updateOptions(options.toString());
        } catch (e: JSONException) {
            return null;
        }
        return connection;
    }

    @Test
    fun echo() = runBlocking {
        val client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        val connection = createStreamConnection(client)!!
        connection.connectAsync()
        val stream = connection.createStream()
        stream.open(42)
        val toWrite = byteArrayOf(42, 32, 44, 45)
        stream.writeAsync(toWrite)
        try {
            val result = stream.readAllAsync(4)
            assertEquals(result.size, 4)
            assertArrayEquals(toWrite, result)
        } catch (e: NabtoEOFException) {
            assert(false)
        }
        stream.close()
        connection.close()
    }

    @Test
    fun echoEOF() = runBlocking {
        val client = NabtoClient.create(InstrumentationRegistry.getInstrumentation().getContext());
        val connection = createStreamConnection(client)!!
        connection.connectAsync()
        val stream = connection.createStream()
        stream.open(42)
        val toWrite = byteArrayOf(42, 32, 44, 45)
        stream.writeAsync(toWrite)
        stream.close()

        try {
            val result = stream.readAllAsync(4)
            assertEquals(result.size, 4)
            assertArrayEquals(toWrite, result)
        } catch (e: NabtoEOFException) {
            assert(false)
        }

        var gotException = false
        try {
            stream.readAllAsync(4)
        } catch (e: NabtoEOFException) {
            gotException = true
        }

        assertTrue(gotException)
        connection.close()
    }
}

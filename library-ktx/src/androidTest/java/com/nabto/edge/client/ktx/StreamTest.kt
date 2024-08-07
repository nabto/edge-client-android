package com.nabto.edge.client.ktx
import com.nabto.edge.client.ktx.test.R

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import kotlinx.coroutines.*

import com.nabto.edge.client.NabtoClient
import com.nabto.edge.client.Connection
import com.nabto.edge.client.NabtoEOFException
import com.nabto.edge.client.*
import org.json.JSONException
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class StreamTest {
    @Test
    fun echo() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().getContext()
        NabtoClient.create(context).use { client ->
            createStreamConnection(client)!!.use { connection ->
                connection.createStream().use { stream ->
                    stream.open(42)
                    val toWrite = byteArrayOf(42, 32, 44, 45)
                    stream.awaitWrite(toWrite)
                    try {
                        val result = stream.awaitReadAll(4)
                        assertEquals(result.size, 4)
                        assertArrayEquals(toWrite, result)
                    } catch (e: NabtoEOFException) {
                        assert(false)
                    }
                    stream.streamClose()
                }
                connection.awaitConnectionClose()
            }
        }
        Unit
    }

    @Test(timeout=10000)
    fun echoEOF() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().getContext()
        NabtoClient.create(context).use { client ->
            createStreamConnection(client)!!.use { connection ->
                connection.createStream().use { stream ->
                    stream.open(42)
                    val toWrite = byteArrayOf(42, 32, 44, 45)
                    stream.awaitWrite(toWrite)
                    stream.streamClose()

                    try {
                        val result = stream.awaitReadAll(4)
                        assertEquals(result.size, 4)
                        assertArrayEquals(toWrite, result)
                    } catch (e: NabtoEOFException) {
                        assert(false)
                    }

                    var gotException = false
                    try {
                        stream.awaitReadAll(4)
                    } catch (e: NabtoEOFException) {
                        gotException = true
                    }

                    assertTrue(gotException)
                    stream.streamClose()
                }
                connection.awaitConnectionClose()
            }
        }
    }

}

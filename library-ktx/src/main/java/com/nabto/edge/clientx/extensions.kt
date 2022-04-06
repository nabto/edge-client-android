package com.nabto.edge.clientx
import com.nabto.edge.client.Connection
import com.nabto.edge.client.Coap
import com.nabto.edge.client.NabtoCallback
import com.nabto.edge.client.Stream
import kotlinx.coroutines.*
import com.nabto.edge.clientx.internal.nabtoCoroutineWrapper

suspend fun Connection.connectAsync() {
    // @TODO: This is kind of awkward, connect doesnt need to return anything so null is passed on the java side
    // so we use Unit? as a nullable type on the kotlin side
    // there must be a nicer way to do this 
    return nabtoCoroutineWrapper<Unit?>(Dispatchers.IO, { callback ->
        this@connectAsync.connectCallback(callback)
    })
}

suspend fun Coap.executeAsync() {
    return nabtoCoroutineWrapper<Unit?>(Dispatchers.IO, { callback ->
        this@executeAsync.executeCallback(callback)
    })
}

suspend fun Stream.openAsync(port: Int) {
    return withContext(Dispatchers.IO) {
        this@openAsync.open(port)
    }
}

suspend fun Stream.readSomeAsync(): ByteArray {
    return withContext(Dispatchers.IO) {
        val job = CompletableDeferred<ByteArray>()
        val callback = object : NabtoCallback<ByteArray> {
            override fun run(array: ByteArray) {
                job.complete(array)
            }
        }
        this@readSomeAsync.readSomeCallback(callback)
        job.join()
        return@withContext job.getCompleted()
    }
}

suspend fun Stream.readAllAsync(length: Int): ByteArray {
    return withContext(Dispatchers.IO) {
        return@withContext this@readAllAsync.readAll(length)
    }
}

suspend fun Stream.writeAsync(bytes: ByteArray) {
    return withContext(Dispatchers.IO) {
        return@withContext this@writeAsync.write(bytes)
    }
}

suspend fun Stream.closeAsync() {
    return withContext(Dispatchers.IO) {
        return@withContext this@closeAsync.close()
    }
}

suspend fun Stream.abortAsync() {
    return withContext(Dispatchers.IO) {
        return@withContext this@abortAsync.abort()
    }
}

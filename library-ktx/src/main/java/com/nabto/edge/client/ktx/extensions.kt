package com.nabto.edge.client.ktx
import com.nabto.edge.client.Connection
import com.nabto.edge.client.Coap
import com.nabto.edge.client.NabtoCallback
import com.nabto.edge.client.Stream
import com.nabto.edge.client.TcpTunnel
import kotlinx.coroutines.*
import com.nabto.edge.client.ktx.internal.*

suspend fun Connection.connectAsync() {
    return nabtoCoroutineWrapper(Dispatchers.IO, { callback ->
        this@connectAsync.connectCallback(callback)
    })
}

suspend fun Coap.executeAsync() {
    return nabtoCoroutineWrapper(Dispatchers.IO, { callback ->
        this@executeAsync.executeCallback(callback)
    })
}

suspend fun Stream.openAsync(port: Int) {
    return nabtoCoroutineWrapper(Dispatchers.IO, { callback ->
        this@openAsync.openCallback(port, callback)
    })
}

suspend fun Stream.readSomeAsync(): ByteArray {
    return nabtoCoroutineWrapperWithReturn<ByteArray>(Dispatchers.IO, { callback ->
        this@readSomeAsync.readSomeCallback(callback)
    })
}

suspend fun Stream.readAllAsync(length: Int): ByteArray {
    return nabtoCoroutineWrapperWithReturn<ByteArray>(Dispatchers.IO, { callback ->
        this@readAllAsync.readAllCallback(length, callback)
    })
}

suspend fun Stream.writeAsync(bytes: ByteArray) {
    return nabtoCoroutineWrapper(Dispatchers.IO, { callback ->
        this@writeAsync.writeCallback(bytes, callback)
    })
}

suspend fun Stream.closeAsync() {
    return nabtoCoroutineWrapper(Dispatchers.IO, { callback ->
        this@closeAsync.closeCallback(callback)
    })
}

suspend fun TcpTunnel.openAsync(service: String, localPort: Int) {
    return nabtoCoroutineWrapper(Dispatchers.IO, { callback ->
        this@openAsync.openCallback(service, localPort, callback)
    })
}

suspend fun TcpTunnel.closeAsync() {
    return nabtoCoroutineWrapper(Dispatchers.IO, { callback ->
        this@closeAsync.closeCallback(callback)
    })
}

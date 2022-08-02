package com.nabto.edge.client.ktx
import java.util.Optional
import com.nabto.edge.client.*
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation

private suspend fun <T> nabtoCoroutineWrapper(
    register: (cb: NabtoCallback<T>) -> Unit
): Optional<T> = suspendCancellableCoroutine<Optional<T>> { continuation ->
    val callback = { error: Int, opt: Optional<T> ->
        if (error == ErrorCodes.OK) {
            continuation.resumeWith(Result.success(opt))
        } else {
            val exception = com.nabto.edge.client.swig.NabtoException(error)
            val cause = when (error) {
                ErrorCodes.END_OF_FILE -> NabtoEOFException(exception)
                // @TODO: We should make a NabtoNoChannelsException here
                //        Not sure how to get the local and remote error codes...
                //        They are internal to the ConnectionImpl class
                ErrorCodes.NO_CHANNELS -> NabtoRuntimeException(exception)
                else -> NabtoRuntimeException(exception)
            }
            continuation.resumeWith(Result.failure(cause))
        }
    }
    register(callback)
}

/**
 * Open a connection as described for Connection.connect().
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the connection
 * is established or an error occurs.
 */
suspend fun Connection.awaitConnect() {
    nabtoCoroutineWrapper<Unit> { callback ->
        this@awaitConnect.connectCallback(callback)
    }
}

/**
 * Execute a CoAP request.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the CoAP
 * result is ready or an error occurs.
 */
suspend fun Coap.awaitExecute() {
    nabtoCoroutineWrapper<Unit> { callback ->
        this@awaitExecute.executeCallback(callback)
    }
}

/**
 * Open a Stream.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the stream is
 * open or an error occurs.
 *
 * @param[port] The stream port to use on the remote server, a streamport is a demultiplexing id.
 */
suspend fun Stream.awaitOpen(port: Int) {
    nabtoCoroutineWrapper<Unit> { callback ->
        this@awaitOpen.openCallback(port, callback)
    }
}

/**
 * Read some bytes from a stream.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the bytes have
 * been read or an error occurs.
 *
 * @return A byte array that was read from the stream.
 */
suspend fun Stream.awaitReadSome(): ByteArray {
    return nabtoCoroutineWrapper<ByteArray>({ callback ->
        this@awaitReadSome.readSomeCallback(callback)
    }).get()
}

/**
 * Read an exact amount of bytes from a stream.
 *
 * This is meant to be used in a Kotlin coroutine to suspend execution until the bytes have been
 * read and returned.
 *
 * @param[length] The amount of to read.
 * @return A byte array that was read from the stream.
 */
suspend fun Stream.awaitReadAll(length: Int): ByteArray {
    return nabtoCoroutineWrapper<ByteArray>({ callback ->
        this@awaitReadAll.readAllCallback(length, callback)
    }).get()
}

/**
 * Write bytes to a stream.
 *
 * This is meant to be used in a Kotlin coroutine to suspend execution until the bytes have been
 * written.
 *
 * @param[bytes] Byte array to be written to the stream.
 */
suspend fun Stream.awaitWrite(bytes: ByteArray) {
    nabtoCoroutineWrapper<Unit> { callback ->
        this@awaitWrite.writeCallback(bytes, callback)
    }
}

/**
 * Close a stream.
 *
 * This is meant to be used in a Kotlin coroutine to suspend execution until the Stream is closed.
 */
suspend fun Stream.awaitClose() {
    nabtoCoroutineWrapper<Unit> { callback ->
        this@awaitClose.closeCallback(callback)
    }
}

/**
 * Open a tunnel.
 *
 * This is meant to be used in a Kotlin coroutine to suspend execution until the TcpTunnel is open.
 */
suspend fun TcpTunnel.awaitOpen(service: String, localPort: Int) {
    nabtoCoroutineWrapper<Unit> { callback ->
        this@awaitOpen.openCallback(service, localPort, callback)
    }
}

/**
 * Close a tunnel.
 *
 * This is meant to be used in a Kotlin coroutine to suspend execution until the TcpTunnel is closed.
 */
suspend fun TcpTunnel.awaitClose() {
    nabtoCoroutineWrapper<Unit> { callback ->
        this@awaitClose.closeCallback(callback)
    }
}

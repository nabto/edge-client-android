package com.nabto.edge.client.ktx
import java.util.Optional
import com.nabto.edge.client.*
import kotlinx.coroutines.*

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
 *
 * @throws NabtoRuntimeException with error code `STOPPED` if the client instance was stopped
 * @throws NabtoRuntimeException with error code `NO_CHANNELS` if a connection could not be established.
 */
suspend fun Connection.awaitConnect() {
    suspendCancellableCoroutine<Optional<Unit>> { continuation ->
        val callback = NabtoCallback<Unit> { error, opt ->
            if (error == ErrorCodes.OK) {
                continuation.resumeWith(Result.success(opt))
            } else {
                val cause = when (error) { 
                    ErrorCodes.NO_CHANNELS -> {
                        NabtoNoChannelsException(
                            this@awaitConnect.localChannelErrorCode.errorCode,
                            this@awaitConnect.remoteChannelErrorCode.errorCode,
                            this@awaitConnect.directCandidatesChannelErrorCode.errorCode
                        )
                    }
                    else -> NabtoRuntimeException(com.nabto.edge.client.swig.NabtoException(error))
                }
                continuation.resumeWith(Result.failure(cause))
            }
        }
        this@awaitConnect.connectCallback(callback);
    }
}

/**
 * Execute a CoAP request.
 *
 * This function is meant to be used in a Kotlin coroutine to suspend execution until the CoAP
 * result is ready or an error occurs.
 *
 * @throws NabtoRuntimeException with error code `TIMEOUT` if the request timed out (took more than 2 minutes.)
 * @throws NabtoRuntimeException with error code `STOPPED` if the coap request or a parent object is stopped.
 * @throws NabtoRuntimeException with error code `NOT_CONNECTED` if the connection is not established yet.
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
 * @param port The stream port to use on the remote server, a streamport is a demultiplexing id.
 * @throws NabtoRuntimeException with error code `STOPPED` if the stream or a parent object was stopped.
 * @throws NabtoRuntimeException with error code `NOT_CONNECTED` if the connection is not established yet.
 */
suspend fun Stream.awaitOpen(
    port: Int,
    ) {
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
 * @throws NabtoRuntimeException With error code `STOPPED` if the stream was stopped.
 * @throws NabtoRuntimeException With error code `OPERATION_IN_PROGRESS` if another read is in progress.
 * @throws NabtoEOFException if eof is reached
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
 * @param length The amount of to read.
 * @throws NabtoRuntimeException With error code `STOPPED` if the stream was stopped.
 * @throws NabtoRuntimeException With error code `OPERATION_IN_PROGRESS` if another read is in progress.
 * @throws NabtoEOFException if end of file is reached.
 * @return A byte array that was read from the stream.
 */
suspend fun Stream.awaitReadAll(
    length: Int,
    ): ByteArray {
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
 * @param bytes Byte array to be written to the stream.
 */
suspend fun Stream.awaitWrite(
    bytes: ByteArray,
    ) {
    nabtoCoroutineWrapper<Unit> { callback ->
        this@awaitWrite.writeCallback(bytes, callback)
    }
}

/**
 * Close a stream.
 *
 * This is meant to be used in a Kotlin coroutine to suspend execution until the Stream is closed.
 *
 * @throws NabtoRuntimeException With error code `STOPPED` if the stream is stopped.
 * @throws NabtoRuntimeException With error code `OPERATION_IN_PROGRESS` if another stop is in progress.
 * @throws NabtoRuntimeException With error code `INVALID_STATE` if the stream is not opened yet.
 */
suspend fun Stream.awaitClose() {
    nabtoCoroutineWrapper<Unit> { callback ->
        this@awaitClose.streamCloseCallback(callback)
    }
}

/**
 * Open a tunnel.
 *
 * This is meant to be used in a Kotlin coroutine to suspend execution until the TcpTunnel is open.
 *
 * @param service The service to connect to on the remote device (as defined in the device's
 * configuration), e.g. "http", "http-admin", "ssh", "rtsp".
 * @param localPort The local port to listen on. If 0 is specified, an ephemeral port is used,
 * it can be retrieved with `getLocalPort()`.
 * @throws NabtoRuntimeException with error code `FORBIDDEN` if the device did not allow opening the tunnel.
 * @throws NabtoRuntimeException with error code `STOPPED` if the tunnel or a parent object is stopped.
 * @throws NabtoRuntimeException with error code `NOT_CONNECTED` if the connection is not established yet.
 */
suspend fun TcpTunnel.awaitOpen(
    service: String,
    localPort: Int,
    ) {
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
        this@awaitClose.tunnelCloseCallback(callback)
    }
}

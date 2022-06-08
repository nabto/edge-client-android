package com.nabto.edge.client.internal
import java.util.Optional
import com.nabto.edge.client.NabtoCallback
import com.nabto.edge.client.ErrorCodes
import com.nabto.edge.client.NabtoException
import com.nabto.edge.client.NabtoRuntimeException
import com.nabto.edge.client.NabtoEOFException
import kotlinx.coroutines.*

// nabtoCoroutineWrapperInternal should be used when the default error handling is not good enough
// @TODO: This function uses experimental coroutines API (the return@withContext), maybe this can be handled in another way?
// @TODO: Maybe it should be named differently since external usage is allowed
suspend fun <T> nabtoCoroutineWrapperInternal(
    dispatcher: CoroutineDispatcher,
    code: (cb: NabtoCallback<T>) -> Unit
): Pair<Int, Optional<T>> {
    return withContext(dispatcher) {
        val job = CompletableDeferred<Pair<Int, Optional<T>>>()
        val callback = object : NabtoCallback<T> {
            override fun run(error: Int, opt: Optional<T>) {
                job.complete(Pair(error, opt))
            }
        }
        code(callback)
        job.join()
        return@withContext job.getCompleted()
    }
}

suspend fun nabtoCoroutineWrapper(
    dispatcher: CoroutineDispatcher,
    code: (cb: NabtoCallback<Unit>) -> Unit
) {
    val (error, _) = nabtoCoroutineWrapperInternal<Unit>(dispatcher, code)
    if (error != ErrorCodes.OK) {
        throw nabtoErrorCodeToException(error)
    }
}

suspend fun <T> nabtoCoroutineWrapperWithReturn(
    dispatcher: CoroutineDispatcher,
    code: (cb: NabtoCallback<T>) -> Unit
): T {
    val (error, opt) = nabtoCoroutineWrapperInternal<T>(dispatcher, code)
    if (error == ErrorCodes.OK) {
        return opt.get()
    } else {
        throw nabtoErrorCodeToException(error)
    }
}

fun nabtoErrorCodeToException(error: Int): Exception {
    val exception = com.nabto.edge.client.swig.NabtoException(error)
    return when (error) {
        ErrorCodes.END_OF_FILE -> NabtoEOFException(exception)
        else -> NabtoRuntimeException(exception)
    }
}

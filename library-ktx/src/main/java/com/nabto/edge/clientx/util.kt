package com.nabto.edge.clientx.internal
import com.nabto.edge.client.NabtoCallback
import kotlinx.coroutines.*

suspend fun <T> nabtoCoroutineWrapper(dispatcher: CoroutineDispatcher, code: (cb: NabtoCallback<T>) -> Unit) {
    return withContext(dispatcher) {
        val job = Job()
        val callback = object : NabtoCallback<T> {
            override fun run(arg: T) {
                job.complete()
            }
        }
        code(callback)
        job.join()
    }
}

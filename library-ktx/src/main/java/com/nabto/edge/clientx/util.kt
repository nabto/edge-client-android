package com.nabto.edge.clientx.internal
import com.nabto.edge.client.NabtoCallback
import kotlinx.coroutines.*

// @TODO: Maybe we should use suspendCoroutine instead?
suspend fun nabtoCoroutineWrapper(dispatcher: CoroutineDispatcher, code: (cb: NabtoCallback<Unit?>) -> Unit) {
    return withContext(dispatcher) {
        val job = Job()

        // @TODO: This is kind of awkward, certain functions dont need to return anything so null is passed on the java side
        // so we use Unit? as a nullable type on the kotlin side
        val callback = object : NabtoCallback<Unit?> {
            override fun run(arg: Unit?) {
                job.complete()
            }
        }

        code(callback)
        job.join()
    }
}

// @TODO: This function uses experimental coroutines API (the return@withContext), maybe this can be handled in another way?
suspend fun <T> nabtoCoroutineWrapperWithReturn(dispatcher: CoroutineDispatcher, code: (cb: NabtoCallback<T>) -> Unit): T {
    return withContext(dispatcher) {
        val job = CompletableDeferred<T>()
        val callback = object : NabtoCallback<T> {
            override fun run(obj: T) {
                job.complete(obj)
            }
        }
        code(callback)
        job.join()
        return@withContext job.getCompleted()
    }
}

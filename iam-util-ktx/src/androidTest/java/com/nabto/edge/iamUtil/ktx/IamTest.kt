package com.nabto.edge.iamutil.ktx

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import kotlinx.coroutines.*
import com.nabto.edge.iamutil.*

import java.lang.Thread
import java.util.concurrent.LinkedBlockingQueue

import android.util.Log
import java.util.Optional

@RunWith(AndroidJUnit4::class)
class IamTest {

    // Somewhat complicated test
    // Here we test that suspendCancellableCoroutine when used in the way that the
    // actual library uses it, it works as we intended.
    // E.g. it suspends a coroutine job until some other thread calls the callback
    // which should resume the coroutine.
    @Test
    fun testSuspendCancellableCoroutine() {
        data class Item(
            val callback: IamCallback<Unit>,
            val error: IamError
        )

        val queue = LinkedBlockingQueue<Item>(10)

        // Consume and run callbacks from the queue
        val thread = Thread {
            try {
                while (true) {
                    val item = queue.take()
                    // simulate working for a bit by sleeping
                    Thread.sleep(1000)
                    item.callback.run(item.error, Optional.empty())
                }
            } catch (e: InterruptedException) {
                // Do nothing and just let the thread die.
            }
        }
        thread.start()

        // All the functions in extensions.kt use iamWrapper in a similar way.
        suspend fun someAsyncFunction(error: IamError = IamError.NONE) {
            iamWrapper<Unit> { callback ->
                queue.put(Item(callback, error))
            }
        }

        var completed = false
        var secondJobCompletedBeforeFirst = false

        // Launch two coroutines, let one of them suspend by calling someAsyncFunction
        // The suspended first job should take a while before it resumes since the thread has a sleep
        // Then the second job should complete before the first
        runBlocking {
            val job1 = launch {
                someAsyncFunction()
                completed = true
            }

            val job2 = launch {
                if (!completed) {
                    secondJobCompletedBeforeFirst = true
                }
            }

            joinAll(job1, job2)
            assertTrue(completed)
            assertTrue(secondJobCompletedBeforeFirst)
        }

        // Test that an exception gets thrown correctly
        runBlocking {
            launch {
                var error = IamError.NONE
                try {
                    someAsyncFunction(IamError.FAILED)
                } catch (e: IamException) {
                    error = e.error
                }
                assertEquals(error, IamError.FAILED)
            }
        }

        thread.interrupt()
    }
}

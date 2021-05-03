package com.rayman.coroutine_await

import androidx.lifecycle.Observer
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.properties.Delegates

/**
 * @author 吕少锐 (lvrayman@gmail.com)
 * @version 4/25/21
 */
class AWaiter<T>(_target: T) {
    var value by Delegates.observable(_target, { property, oldValue, newValue ->
        notifyAllObserver()
    })

    private val observers = ConcurrentHashMap<String, Observer<T>>()

    fun update(newValue: T) {
        value = newValue
    }

    fun update(invocation: (T) -> Unit) {
        invocation(value)
        notifyAllObserver()
    }

    @Synchronized
    private fun notifyAllObserver() {
        observers.values.forEach {
            it.onChanged(value)
        }
    }

    @Synchronized
    private fun removeObserver(key: String) {
        observers.remove(key)
    }

    @Synchronized
    private fun addObserver(key: String, observer: Observer<T>) {
        observers[key] = observer
    }

    suspend fun awaitTimeout(timeout: Long, predicate: (T) -> Boolean) = withTimeout(timeout) {
        await(0, predicate)
    }

    suspend fun await(
        timeout: Long = 0,
        predicate: (T) -> Boolean) = suspendCancellableCoroutine<Boolean> { cont ->
        if (predicate(value)) {
            cont.resume(true)
        }
        var timeoutJob: Job? = null
        val key = predicate.hashCode().toString()
        val observer: Observer<T> = Observer { newValue ->
            if (predicate(newValue)) {
                cont.resume(true)
                if (timeoutJob?.isActive == true) timeoutJob?.cancel()
                removeObserver(key)
            }
        }
        addObserver(key, observer)

        if (timeout > 0) {
            timeoutJob = MainScope().launch {
                delay(timeout)
                removeObserver(key)
                if (cont.isActive) {
                    cont.resume(false)
                }
            }
        }

        cont.invokeOnCancellation {
            if (timeoutJob?.isActive == true) timeoutJob.cancel()
            removeObserver(key)
        }
    }

}
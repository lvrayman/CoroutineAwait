package com.rayman.coroutine_await

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.*
import java.lang.ref.SoftReference
import kotlin.coroutines.resume

/**
 * @author 吕少锐 (lvrayman@gmail.com)
 * @version 3/31/21
 */
suspend fun <T> LiveData<T?>.awaitTimeout(
    lifecycleOwner: LifecycleOwner,
    timeout: Long,
    predicate: (T?) -> Boolean) = withTimeout(timeout) {
    await(lifecycleOwner, 0, predicate)
}

suspend fun <T> LiveData<T?>.await(
    lifecycleOwner: LifecycleOwner,
    timeout: Long = 0,
    predicate: (T?) -> Boolean) = suspendCancellableCoroutine<Boolean> { cont ->
    var observerRef: SoftReference<Observer<Any?>>? = null
    var timeoutJob: Job? = null
    if (timeout > 0) {
        timeoutJob = MainScope().launch {
            delay(timeout)
            observerRef?.get()?.let { observer ->
                removeObserver(observer)
            }
            if (cont.isActive) {
                cont.resume(false)
            }
        }
    }
    val observer = Observer<Any?> {
        if (predicate(value)) {
            observerRef?.get()?.let { observer ->
                removeObserver(observer)
            }
            if (timeoutJob?.isActive == true) timeoutJob.cancel()
            if (cont.isActive) {
                cont.resume(true)
            }
        }
    }
    observerRef = SoftReference(observer)
    observe(lifecycleOwner, observer)
    cont.invokeOnCancellation {
        if (timeoutJob?.isActive == true) timeoutJob.cancel()
        MainScope().launch {
            removeObserver(observer)
        }
    }
}

fun <T> MutableLiveData<T?>.update(invocationHandler: (T?) -> Unit) {
    val newValue = value
    invocationHandler(newValue)
    MainScope().launch {
        value = newValue
    }
}
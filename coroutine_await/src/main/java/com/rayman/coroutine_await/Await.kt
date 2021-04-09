package com.rayman.coroutine_await

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.rayman.r_utils.log.RLog
import kotlinx.coroutines.*
import java.lang.ref.SoftReference
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author 吕少锐 (lvrayman@gmail.com)
 * @version 3/31/21
 */
suspend fun <T> LiveData<T?>.awaitTimeout(
    lifecycleOwner: LifecycleOwner,
    timeout: Long = 0,
    predicate: (T?) -> Boolean) = withTimeout(timeMillis = timeout) {
    await(lifecycleOwner, 0, predicate)
}

suspend fun <T> LiveData<T?>.await(
    lifecycleOwner: LifecycleOwner,
    timeout: Long = 0,
    predicate: (T?) -> Boolean) = suspendCancellableCoroutine<Boolean> { cont ->
    var observerRef: SoftReference<Observer<Any?>>? = null
    var timeoutJob: Job? = null
    if (timeout > 0) {
        timeoutJob = InnerScope.instance.launch(Dispatchers.Main) {
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
        RLog.info("LiveData/await/cancel")
        if (timeoutJob?.isActive == true) timeoutJob.cancel()
        InnerScope.instance.launch(Dispatchers.Main) {
            removeObserver(observer)
        }
    }
}


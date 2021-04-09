package com.rayman.coroutine_await

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * @author 吕少锐 (lvrayman@gmail.com)
 * @version 4/4/21
 */
internal class InnerScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    companion object {
        @Volatile
        private var _instance: InnerScope? = null

        val instance: InnerScope
            get() = _instance ?: synchronized(this) {
                _instance ?: InnerScope()
            }
    }
}
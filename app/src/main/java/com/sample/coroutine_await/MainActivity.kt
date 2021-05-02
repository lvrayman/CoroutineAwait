package com.sample.coroutine_await

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.rayman.coroutine_await.AWaiter
import com.rayman.coroutine_await.await
import com.rayman.coroutine_await.awaitTimeout
import com.rayman.r_utils.log.RLog
import com.sample.coroutine_await.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val vb by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val person = AWaiter(Person("rayman", 19))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)
        RLog.setTag("coroutine_await_log")

        val job = lifecycleScope.launchWhenResumed {
            RLog.info("MainActivity/onCreate/wait start")
            val awaitSuccess = person.await(2000) { it.age == 20 }
            RLog.info("MainActivity/onCreate/wait finish------------",
                "awaitSuccess", awaitSuccess)
        }

        vb.btn.setOnClickListener {
            person.update { it.age = 20 }
        }
    }

}
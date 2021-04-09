package com.sample.coroutine_await

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.rayman.coroutine_await.await
import com.rayman.coroutine_await.awaitTimeout
import com.rayman.r_utils.log.RLog
import com.sample.coroutine_await.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }
    private val vb by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)
        RLog.setTag("coroutine_await_log")
        viewModel.personLiveData.postValue(Person("rayman", 18))

        val job = lifecycleScope.launchWhenResumed {
            RLog.info("MainActivity/onCreate/wait start")
//            val awaitSuccess = viewModel.personLiveData.await(this@MainActivity) {
//                it?.age == 20
//            }
            viewModel.personLiveData.awaitTimeout(lifecycleOwner = this@MainActivity,
                timeout = 2_000) {
                it?.age == 20
            }
            RLog.info("MainActivity/onCreate/wait finish------------",
//                "waitSuccess", awaitSuccess,
                "hasObserver", viewModel.personLiveData.hasActiveObservers())
        }

        lifecycleScope.launch {
            delay(3000)
            RLog.info("MainActivity/onCreate",
                "isActive", job.isActive)
        }

        vb.btn.setOnClickListener {
            viewModel.personLiveData.value?.also {
                it.age++
            }.apply {
                viewModel.personLiveData.postValue(this)
            }
            RLog.info("MainActivity/onCreate/onClick",
                "age", viewModel.personLiveData.value?.age)
        }
    }

}
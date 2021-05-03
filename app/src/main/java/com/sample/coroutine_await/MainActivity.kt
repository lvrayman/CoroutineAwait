package com.sample.coroutine_await

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.rayman.coroutine_await.AWaiter
import com.rayman.coroutine_await.await
import com.rayman.coroutine_await.update
import com.rayman.r_utils.log.RLog
import com.sample.coroutine_await.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val vb by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val person = AWaiter(Person("rayman", 19))
    private val viewModel: MainViewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)
        RLog.setTag("coroutine_await_log")

        lifecycleScope.launchWhenResumed {
            person.await { it.age == 20 }
            Toast.makeText(applicationContext, "Now my age is 20!", Toast.LENGTH_SHORT).show()
        }

        viewModel.personLiveData.value = person.value
        lifecycleScope.launchWhenResumed {
            viewModel.personLiveData.await(this@MainActivity) {
                it?.name == "handsome ray"
            }
            Toast.makeText(applicationContext, "Now my handsome ray", Toast.LENGTH_SHORT).show()
        }

        vb.btnAge.setOnClickListener {
            person.update { it.age = 20 }
        }

        vb.btnName.setOnClickListener {
            viewModel.personLiveData.update { it?.name = "handsome ray" }
        }
    }

}
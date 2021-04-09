package com.sample.coroutine_await

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author 吕少锐 (lvrayman@gmail.com)
 * @version 4/1/21
 */
class MainViewModel : ViewModel() {
    val intLiveData = MutableLiveData<Int>(0)
    val personLiveData = MutableLiveData<Person>()
}
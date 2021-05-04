## 简介

CoroutineAwait是利用kotlin协程进行状态等待的一个工具库，可将当前协程挂起直到某个变量的值变成期待值时恢复运行。

## 使用

对于LiveData对象，使用扩展方法`await`即可进行：

```kotlin
lifecycleScope.launch {
    viewModel.personLiveData.await(this@MainActivity) {
        it?.name == "handsome ray"
    }
    Toast.makeText(applicationContext, "Now I'm handsome ray", Toast.LENGTH_SHORT).show()
}
```

`await`扩展方法有类型为Boolean的返回值，可设置超时时间，当等待完成时返回值为true，超时时返回值为false，并继续往下执行

当使用`awaitTimeout`方法，在超时时会将当前协程取消

LiveData的await方法接收LifecycleOwner对象



对于非LiveData对象，可用`AWaiter`进行包装

```kotlin
val person = AWaiter(Person("rayman", 19))
lifecycleScope.launch {
    person.await { it.age == 20 }
    Toast.makeText(applicationContext, "Now my age is 20!", Toast.LENGTH_SHORT).show()
}
```

使用update进行数据更新

```kotlin
person.update { it.age = 20 }
```

await方法同样返回是否等待成功的Boolean值，以及取消协程的`awaitTimeout`方法
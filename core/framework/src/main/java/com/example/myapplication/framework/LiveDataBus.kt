package com.example.myapplication.framework

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.ConcurrentHashMap

/**
 * 进程内跨页面事件总线（基于 LiveData，主线程 [post]/[set]）。
 *
 * 注意：跨进程、超大 payload、高频事件请改用显式接口或 Flow；粘性事件可扩展 [sticky] 分支。
 */
object LiveDataBus {

    private val channels = ConcurrentHashMap<String, MutableLiveData<Any?>>()

    @Suppress("UNCHECKED_CAST")
    fun <T> with(key: String): MutableLiveData<T?> {
        return channels.getOrPut(key) { MutableLiveData() } as MutableLiveData<T?>
    }

    fun post(key: String, value: Any?) {
        with<Any>(key).postValue(value)
    }

    fun <T> observe(
        owner: LifecycleOwner,
        key: String,
        observer: Observer<in T?>,
    ) {
        with<T>(key).observe(owner, observer)
    }

    fun <T> observeOnce(owner: LifecycleOwner, key: String, observer: Observer<in T?>) {
        val live = with<T>(key)
        val wrapped = object : Observer<T?> {
            override fun onChanged(value: T?) {
                live.removeObserver(this)
                observer.onChanged(value)
            }
        }
        live.observe(owner, wrapped)
    }
}

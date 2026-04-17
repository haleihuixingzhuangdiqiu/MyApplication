package com.example.myapplication.network

/** 由 App 注入（如对接 Timber），便于 core 层不依赖具体日志实现。 */
fun interface NetworkLogger {
    fun log(tag: String, message: String)
}

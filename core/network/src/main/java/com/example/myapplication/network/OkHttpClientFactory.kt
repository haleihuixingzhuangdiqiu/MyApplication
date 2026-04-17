package com.example.myapplication.network

import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * 统一 OkHttp 超时、缓存目录与拦截器顺序（应用拦截器 → 网络层）。
 */
object OkHttpClientFactory {

    fun create(
        cache: Cache,
        applicationInterceptors: List<Interceptor> = emptyList(),
        networkInterceptors: List<Interceptor> = emptyList(),
        connectTimeoutSec: Long = 20,
        readTimeoutSec: Long = 20,
        writeTimeoutSec: Long = 20,
    ): OkHttpClient {
        val b = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(connectTimeoutSec, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSec, TimeUnit.SECONDS)
            .writeTimeout(writeTimeoutSec, TimeUnit.SECONDS)
        applicationInterceptors.forEach { b.addInterceptor(it) }
        networkInterceptors.forEach { b.addNetworkInterceptor(it) }
        return b.build()
    }
}

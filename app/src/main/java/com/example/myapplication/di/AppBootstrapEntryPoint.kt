package com.example.myapplication.di

import com.example.myapplication.network.ApiBusinessErrorHubStartup
import com.example.myapplication.network.GlobalBusinessErrorEventPipeline
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.components.SingletonComponent

/**
 * 在 [com.example.myapplication.startup.LibrariesStartupInitializer] 中通过
 * [dagger.hilt.android.EarlyEntryPoints] 在 [android.app.Application.onCreate] 之前
 * 触达，用于尽早注册 [ApiBusinessErrorHubStartup]、启动 [GlobalBusinessErrorEventPipeline] 订阅（与 [CoilImageLoaderEntryPoint] 同方式）。
 */
@EarlyEntryPoint
@InstallIn(SingletonComponent::class)
interface AppBootstrapEntryPoint {
    fun apiBusinessErrorHubStartup(): ApiBusinessErrorHubStartup
    fun globalBusinessErrorEventPipeline(): GlobalBusinessErrorEventPipeline
}

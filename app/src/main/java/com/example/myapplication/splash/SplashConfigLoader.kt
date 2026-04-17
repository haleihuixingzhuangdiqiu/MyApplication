package com.example.myapplication.splash

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import timber.log.Timber

/** 模拟首启拉取远端配置（如 Firebase Remote Config）；由 [com.example.myapplication.SplashActivity] 并行触发。 */
object SplashConfigLoader {

    /** 模拟网络 + 解析耗时约 0.75s（在 IO 调度器上，便于与主线程绘制解耦；支持协程取消）。 */
    suspend fun mockLoadRemoteConfig() {
        withContext(Dispatchers.IO) {
            ensureActive()
            delay(750)
            Timber.tag("Splash").d("mock remote config loaded (demo)")
        }
    }
}

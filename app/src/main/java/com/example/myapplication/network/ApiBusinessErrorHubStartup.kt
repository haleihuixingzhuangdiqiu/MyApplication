package com.example.myapplication.network

import com.example.myapplication.network.result.ApiBusinessErrorHub
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 在 [ApiBusinessErrorHub] 中挂载业务码侧链插件；由 Hilt 单例化时 [init] 即完成 [register]。
 * 在 [com.example.myapplication.startup.LibrariesStartupInitializer] 中经 [com.example.myapplication.di.AppBootstrapEntryPoint] 强引用创建，避免从未注入则 Hub 无插件。
 */
@Singleton
class ApiBusinessErrorHubStartup @Inject constructor(
    plugin: LogoutOnUnauthorizedPlugin,
) {
    init {
        ApiBusinessErrorHub.register(plugin)
    }
}

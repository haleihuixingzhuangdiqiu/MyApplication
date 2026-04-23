package com.example.myapplication.network

import android.app.Application
import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.network.result.BusinessErrorCodes
import com.example.myapplication.network.result.GlobalBusinessErrorEvents
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App 内**单点**消费 [GlobalBusinessErrorEvents]：在 Hub/插件之后收到业务码，**此处**决定导航、埋点等。
 * 需要新增其它 code 的响应，可在 `when (e.code)` 中扩展，或再注册第二路 collector（同一 [GlobalBusinessErrorEvents.events] 可多订阅，需自管协程）。
 */
@Singleton
class GlobalBusinessErrorEventPipeline @Inject constructor(
    @param:ApplicationContext private val app: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            GlobalBusinessErrorEvents.events.collect { e ->
                when (e.code) {
                    BusinessErrorCodes.UNAUTHORIZED_LOGOUT -> withContext(Dispatchers.Main) {
                        ARouter.getInstance().build(RoutePaths.LOGIN).withFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP,
                            ).navigation(app)
                    }

                    else -> Unit
                }
            }
        }
    }
}

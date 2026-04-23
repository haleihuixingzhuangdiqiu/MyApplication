package com.example.myapplication.network

import com.example.myapplication.network.result.ApiResult
import com.example.myapplication.network.result.BusinessErrorCodes
import com.example.myapplication.network.result.OnBusinessCodePlugin
import com.example.myapplication.session.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 在 [com.example.myapplication.network.result.ApiBusinessErrorHub] 中注册后，
 * 任意 [com.example.myapplication.network.result.handleResult] 收到 [BusinessErrorCodes.UNAUTHORIZED_LOGOUT] 时清本地会话（与页面无关）。
 * 要新增其它 code 的侧链，应再实现 [com.example.myapplication.network.result.ApiBusinessErrorPlugin] 并 [register]。
 */
@Singleton
class LogoutOnUnauthorizedPlugin @Inject constructor(
    private val session: SessionRepository,
) : OnBusinessCodePlugin(BusinessErrorCodes.UNAUTHORIZED_LOGOUT) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onMatched(error: ApiResult.BusinessError) {
        scope.launch { session.signOut() }
    }
}

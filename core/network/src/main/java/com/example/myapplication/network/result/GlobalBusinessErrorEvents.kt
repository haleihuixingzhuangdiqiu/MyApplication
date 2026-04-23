package com.example.myapplication.network.result

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 在 [ApiBusinessErrorHub] 对 [ApiResult.BusinessError] 执行 [ApiBusinessErrorPlugin] **之后**，
 * 将同一条业务错 **再发一份** 到 [events]，供 App **单点** 订阅（导航、埋点、日志等）；
 * 与「插件里做清 Session」正交——**具体怎么响应由 App 里对 [events] 的 collect 决定**。
 *
 * 业务里用 [handleResult] 时**不必**在页面再写 `-1001` 登出；需要统一跳登录时只接 [events] 即可。
 */
object GlobalBusinessErrorEvents {

    private val _events = MutableSharedFlow<GlobalBusinessErrorEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * Hub [ApiBusinessErrorHub.dispatch] 在**所有** [ApiBusinessErrorPlugin] 跑完后发出；一次业务错对应一次事件。
     */
    val events: SharedFlow<GlobalBusinessErrorEvent> = _events.asSharedFlow()

    internal fun emitAfterHubDispatch(error: ApiResult.BusinessError) {
        _events.tryEmit(
            GlobalBusinessErrorEvent(
                code = error.code,
                message = error.message,
            ),
        )
    }
}

/**
 * 与 [ApiResult.BusinessError] 字段一致，便于在 App 层 `when (e.code)` 或走 [BusinessErrorCodes]。
 */
data class GlobalBusinessErrorEvent(
    val code: Int,
    val message: String?,
)

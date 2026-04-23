package com.example.myapplication.network.result

/**
 * 与后端**约定**的「业务级」错误码（与 HTTP 4xx/5xx 无关，来自 JSON 的 [ApiResponse.code]）。
 *
 * [handleResult] 解出 [ApiResult.BusinessError] 后会经 [withNotifiedGlobalBusinessError] → [ApiBusinessErrorHub]；
 * 再经 [GlobalBusinessErrorEvents] 对 App **单点流出** [GlobalBusinessErrorEvent]；同步侧链见 [ApiBusinessErrorPlugin]（如清会话）。
 * 与链式「Hub 优先生效码」的交集见 [RequestChainErrorDispatch.hubFirstBusinessCodes]。
 */
object BusinessErrorCodes {
    /**
     * 与后端约定：例如 token 失效、被踢下线。可注册 [ApiBusinessErrorPlugin] 在 Hub 中统一清会话/跳登录，不必各页面散写。
     */
    const val UNAUTHORIZED_LOGOUT: Int = -1001
}

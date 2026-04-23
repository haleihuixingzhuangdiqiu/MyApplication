package com.example.myapplication.network.result

import java.util.concurrent.CopyOnWriteArrayList

/**
 * 在 [handleResult] 等封装层统一「业务码侧链」：成功解包为 [ApiResult.BusinessError] 时，
 * 按注册顺序回调所有 [ApiBusinessErrorPlugin]；**然后**发 [GlobalBusinessErrorEvents]（给 App 单点接导航/埋点，与插件正交）。
 *
 * 注册 [ApiBusinessErrorPlugin] 见 [register]；**响应事件**见 [GlobalBusinessErrorEvents]。
 */
object ApiBusinessErrorHub {

    private val plugins = CopyOnWriteArrayList<ApiBusinessErrorPlugin>()

    /** 主线程/线程要求见 [ApiBusinessErrorPlugin] */
    @Synchronized
    fun register(plugin: ApiBusinessErrorPlugin) {
        if (plugin !in plugins) plugins.add(plugin)
    }

    @Synchronized
    fun unregister(plugin: ApiBusinessErrorPlugin) {
        plugins.remove(plugin)
    }

    fun clear() {
        plugins.clear()
    }

    /**
     * 由 [handleResult] 与 [withNotifiedGlobalBusinessError] 在得到 [ApiResult.BusinessError] 后调用；勿在业务里重复调，除非自己做了裸 [toApiResult] 且想对齐行为。
     */
    fun dispatch(error: ApiResult.BusinessError) {
        for (p in plugins) {
            if (p.handles(error.code, error.message)) {
                p.onError(error)
            }
        }
        GlobalBusinessErrorEvents.emitAfterHubDispatch(error)
    }
}

/**
 * 全局业务码侧链：若 [handles] 为 true，则 [onError] 在封装层被调用，无需页面再写分支。
 * [onError] 默认在 [handleResult] 的 [kotlinx.coroutines.CoroutineDispatcher] 上**同步**执行（常见为 IO 线程）；
 * 要更新 UI/导航请自行切主线程或 `viewModelScope.launch`。
 */
interface ApiBusinessErrorPlugin {

    fun handles(code: Int, message: String?): Boolean

    fun onError(error: ApiResult.BusinessError)
}

/** 只关心一个 code 时的便捷基类（[handles] 已限定，[onMatched] 内不必再判 code）。 */
abstract class OnBusinessCodePlugin(
    private val expectedCode: Int,
) : ApiBusinessErrorPlugin {

    final override fun handles(code: Int, message: String?): Boolean = code == expectedCode

    final override fun onError(error: ApiResult.BusinessError) {
        onMatched(error)
    }

    protected abstract fun onMatched(error: ApiResult.BusinessError)
}

/**
 * 与 [handleResult] 等价的「对业务错的全局侧链」：在任意得到 [ApiResult] 后调一次，避免裸 [toApiResult] 漏掉。
 */
fun <T> ApiResult<T>.withNotifiedGlobalBusinessError(): ApiResult<T> = apply {
    if (this is ApiResult.BusinessError) {
        ApiBusinessErrorHub.dispatch(this)
    }
}

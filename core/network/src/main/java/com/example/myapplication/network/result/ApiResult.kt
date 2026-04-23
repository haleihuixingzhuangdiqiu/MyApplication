package com.example.myapplication.network.result

/**
 * 统一网络/业务结果：成功带数据；业务失败（HTTP 成功但 code 非期望）；网络/解析等异常。
 *
 * [NetworkError] 为 `ApiResult<Nothing>`，可安全作为任意 `ApiResult<T>` 返回（协变）。
 */
sealed class ApiResult<out T> {

    data class Success<T>(val data: T) : ApiResult<T>()

    data class BusinessError(
        val code: Int,
        val message: String?,
    ) : ApiResult<Nothing>()

    data class NetworkError(
        val cause: Throwable,
        val message: String? = null,
    ) : ApiResult<Nothing>() {
        fun userMessageOrDefault(): String =
            message ?: cause.message.orEmpty().ifBlank { cause::class.simpleName.orEmpty() }
    }
}

inline fun <T> ApiResult<T>.fold(
    onSuccess: (T) -> Unit,
    onBusinessError: (Int, String?) -> Unit = { _, _ -> },
    onNetworkError: (Throwable, String?) -> Unit = { _, _ -> },
) {
    when (this) {
        is ApiResult.Success -> onSuccess(data)
        is ApiResult.BusinessError -> onBusinessError(code, message)
        is ApiResult.NetworkError -> onNetworkError(cause, message)
    }
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.BusinessError -> this
    is ApiResult.NetworkError -> this
}

/**
 * 失败时用于 Toast 等的一条可读文案；**成功** 时为 `null`。
 * [ApiResult.BusinessError] 在 [ApiResult.BusinessError.message] 为空时回退为「操作失败（code）」。
 */
fun <T> ApiResult<T>.failureMessageOrNull(): String? = when (this) {
    is ApiResult.Success -> null
    is ApiResult.BusinessError ->
        message?.takeIf { it.isNotBlank() } ?: "操作失败（$code）"
    is ApiResult.NetworkError -> userMessageOrDefault()
}

/**
 * 只写成功分支；失败时由 [shouldNotify] 决定是否调用 [notify]（一般传 `postError` / `showToast`）。
 * 与 [ApiBusinessErrorHub] 等配合时，可对特定 [ApiResult.BusinessError.code] 令 [shouldNotify] 为 `false`，避免与全局处理重复弹 Toast。
 */
inline fun <T> ApiResult<T>.onSuccessOrNotify(
    noinline notify: (String) -> Unit,
    crossinline shouldNotify: (ApiResult<T>) -> Boolean = { it !is ApiResult.Success },
    crossinline onSuccess: (T) -> Unit,
) {
    when (this) {
        is ApiResult.Success -> onSuccess(data)
        else -> {
            if (shouldNotify(this)) {
                failureMessageOrNull()?.let { msg -> notify(msg) }
            }
        }
    }
}

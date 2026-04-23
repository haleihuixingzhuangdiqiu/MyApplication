package com.example.myapplication.network.result

/**
 * 配合 [kotlinx.coroutines.flow.Flow] 或 ViewModel 内 [kotlinx.coroutines.flow.MutableStateFlow] 使用的一次性加载状态。
 */
sealed class ApiState<out T> {

    data object Loading : ApiState<Nothing>()

    data class Success<T>(val data: T) : ApiState<T>()

    data class Error(
        val cause: Throwable,
        val message: String? = null,
    ) : ApiState<Nothing>() {
        fun userMessageOrDefault(): String =
            message ?: cause.message.orEmpty().ifBlank { cause::class.simpleName.orEmpty() }
    }
}

fun <T> ApiResult<T>.toApiState(): ApiState<T> = when (this) {
    is ApiResult.Success -> ApiState.Success(data)
    is ApiResult.BusinessError -> ApiState.Error(
        cause = BusinessException(code, message),
        message = message,
    )
    is ApiResult.NetworkError -> ApiState.Error(cause = cause, message = message)
}

/** 业务码非成功时用于 [ApiState.Error] 的可识别异常（便于测试/分类）。 */
class BusinessException(
    val code: Int,
    override val message: String?,
) : Exception("code=$code message=$message")

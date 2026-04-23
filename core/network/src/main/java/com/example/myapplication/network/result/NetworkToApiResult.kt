package com.example.myapplication.network.result

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * |  |  |
 * |--|--|
 * | [handleData] | JSON/body **直接**成业务数据，无 `code` 包一层 → [ApiResult] |
 * | [handleResult] | 后端统一成 [ApiResponse]（`code/message/data`）→ [ApiResult] + Hub 侧链 |
 * | [handleDataFlow] / [handleResultFlow] | 与上**同一类接口**，多 `Flow<ApiState>` 且带头 `Loading` |
 * | [Flow.asState] | 已有 [Flow]（如 Room）映 [ApiState]，不另发 Loading |
 */

/**
 * 无 code 壳：成功即 `T`，失败 [ApiResult.NetworkError]。
 */
suspend fun <T> handleData(
    context: CoroutineContext = EmptyCoroutineContext,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    messageMapper: NetworkErrorMessageMapper = DefaultNetworkErrorMessageMapper,
    call: suspend () -> T,
): ApiResult<T> = withContext(context + dispatcher) {
    runCatching { call() }
        .fold(
            onSuccess = { ApiResult.Success(it) },
            onFailure = { it.toNetworkApiResult(messageMapper) as ApiResult<T> },
        )
}

/**
 * 有 [ApiResponse] 包装：[toApiResult] 判 `code`；若 [dispatchGlobalBusiness] 为 true，再经
 * [withNotifiedGlobalBusinessError] 进 Hub 与 [GlobalBusinessErrorEvents]；为 false 时仅得 [ApiResult.BusinessError]，
 * 不触发全局插件/事件（如页内自处理业务错）。
 */
suspend fun <T> handleResult(
    context: CoroutineContext = EmptyCoroutineContext,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    successIf: (Int) -> Boolean = { it == 0 },
    allowNullData: Boolean = false,
    messageMapper: NetworkErrorMessageMapper = DefaultNetworkErrorMessageMapper,
    dispatchGlobalBusiness: Boolean = true,
    call: suspend () -> ApiResponse<T>,
): ApiResult<T> = withContext(context + dispatcher) {
    runCatching { call() }
        .fold(
            onSuccess = { response ->
                val r = response.toApiResult(successIf = successIf, allowNullData = allowNullData)
                if (dispatchGlobalBusiness) r.withNotifiedGlobalBusinessError() else r
            },
            onFailure = { it.toNetworkApiResult(messageMapper) as ApiResult<T> },
        )
}

/**
 * 与 [handleData] 同场景，`Flow` 先 [ApiState.Loading] 再终态；[call] 里勿再包 [handleData]。
 */
fun <T> handleDataFlow(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    messageMapper: NetworkErrorMessageMapper = DefaultNetworkErrorMessageMapper,
    call: suspend () -> T,
): Flow<ApiState<T>> = flow {
    emit(ApiState.Loading)
    emit(
        handleData(
            dispatcher = dispatcher,
            messageMapper = messageMapper,
            call = call,
        ).toApiState(),
    )
}.flowOn(dispatcher)

/**
 * 与 [handleResult] 同场景，`Flow` 先 Loading 再终态；[call] 里勿再包 [handleResult]。
 */
fun <T> handleResultFlow(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    successIf: (Int) -> Boolean = { it == 0 },
    allowNullData: Boolean = false,
    messageMapper: NetworkErrorMessageMapper = DefaultNetworkErrorMessageMapper,
    dispatchGlobalBusiness: Boolean = true,
    call: suspend () -> ApiResponse<T>,
): Flow<ApiState<T>> = flow {
    emit(ApiState.Loading)
    emit(
        handleResult(
            dispatcher = dispatcher,
            successIf = successIf,
            allowNullData = allowNullData,
            messageMapper = messageMapper,
            dispatchGlobalBusiness = dispatchGlobalBusiness,
            call = call,
        ).toApiState(),
    )
}.flowOn(dispatcher)

/**
 * 数据 [Flow] → [ApiState]；[catch] 为 Error；**不**发 Loading。与 [handleDataFlow] / [handleResultFlow] 不同。
 */
fun <T> Flow<T>.asState(
    messageMapper: NetworkErrorMessageMapper = DefaultNetworkErrorMessageMapper,
): Flow<ApiState<T>> =
    map { data: T -> ApiState.Success(data) as ApiState<T> }
        .catch { t ->
            @Suppress("UNCHECKED_CAST")
            emit(ApiState.Error(t, messageMapper.map(t)) as ApiState<T>)
        }

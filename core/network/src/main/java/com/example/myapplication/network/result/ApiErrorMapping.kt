package com.example.myapplication.network.result

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * 将异常映射为对用户/埋点更友好的短文案；可在应用层替换为「从 stringRes 读取」的实现。
 */
fun interface NetworkErrorMessageMapper {
    fun map(throwable: Throwable): String?
}

object DefaultNetworkErrorMessageMapper : NetworkErrorMessageMapper {
    override fun map(throwable: Throwable): String? = when (throwable) {
        is HttpException -> "http ${throwable.code()}"
        is UnknownHostException -> "network unreachable"
        is SocketTimeoutException -> "request timeout"
        is SSLException -> "ssl error"
        is IOException -> "io error"
        else -> null
    }
}

fun Throwable.toNetworkApiResult(
    messageMapper: NetworkErrorMessageMapper = DefaultNetworkErrorMessageMapper,
): ApiResult<Nothing> = ApiResult.NetworkError(
    cause = this,
    message = messageMapper.map(this),
)

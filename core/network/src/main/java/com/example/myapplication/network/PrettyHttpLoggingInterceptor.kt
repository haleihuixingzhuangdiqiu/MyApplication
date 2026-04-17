package com.example.myapplication.network

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 打印请求 URL / Header；响应 Code / Header / Body（[Response.peekBody] 不消费真实流）。
 * JSON 响应尝试 Gson 美化，便于在 Logcat 中阅读出参。
 */
class PrettyHttpLoggingInterceptor(
    private val logger: NetworkLogger,
    private val maxBodyLogBytes: Long = 256 * 1024,
) : Interceptor {

    private val gsonPretty = GsonBuilder().setPrettyPrinting().serializeNulls().create()
    private val tag = "HttpPretty"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val reqLog = StringBuilder()
        reqLog.appendLine("── ${request.method} ${request.url} ──")
        appendHeaders(reqLog, request.headers)
        if (request.body != null) {
            reqLog.appendLine("(request 含 Body，不在此处读取以免破坏 OkHttp 单次 Body 流)")
        }
        logger.log(tag, reqLog.toString())

        val response = chain.proceed(request)
        val out = StringBuilder()
        out.appendLine("── ${response.code} ${response.message} ${request.url} ──")
        appendHeaders(out, response.headers)
        try {
            val peeked = response.peekBody(maxBodyLogBytes)
            val bodyString = peeked.string()
            out.appendLine("-- response body (peek) --")
            out.appendLine(prettyJsonIfPossible(bodyString))
        } catch (_: Exception) {
            out.appendLine("(response body: 无法 peek)")
        }
        logger.log(tag, out.toString())
        return response
    }

    private fun appendHeaders(sb: StringBuilder, headers: Headers) {
        if (headers.size == 0) return
        sb.appendLine("-- headers --")
        for (i in 0 until headers.size) {
            sb.appendLine("${headers.name(i)}: ${headers.value(i)}")
        }
    }

    private fun prettyJsonIfPossible(body: String): String {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return "(empty)"
        return try {
            val el = JsonParser.parseString(trimmed)
            gsonPretty.toJson(el)
        } catch (_: Exception) {
            trimmed
        }
    }
}

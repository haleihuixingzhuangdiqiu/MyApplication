package com.example.myapplication.network.result

import com.google.gson.annotations.SerializedName

/**
 * 常见业务 JSON 包裹：`{ "code":0, "message":"…", "data": … }`。
 * 若后端使用 `result` 字段，可再建一个 DTO 或在本类上增加字段 + 自定义解析。
 */
data class ApiResponse<T>(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
)

/**
 * @param successIf 判断业务是否成功，默认 `code == 0`。
 * @param allowNullData 成功时 `data == null` 是否仍视为 [ApiResult.Success]（如 void 接口）。
 *
 * 若**未**经 [handleResult] 而直接调用本方法，需与封装层一致的全局侧链时，对 [ApiResult] 使用 [withNotifiedGlobalBusinessError]。
 */
fun <T> ApiResponse<T>.toApiResult(
    successIf: (Int) -> Boolean = { it == 0 },
    allowNullData: Boolean = false,
): ApiResult<T> {
    if (!successIf(code)) {
        return ApiResult.BusinessError(code, message)
    }
    val payload = data
    if (payload == null && !allowNullData) {
        return ApiResult.BusinessError(code, message ?: "empty data")
    }
    @Suppress("UNCHECKED_CAST")
    return ApiResult.Success(payload as T)
}

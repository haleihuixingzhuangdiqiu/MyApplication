package com.example.myapplication.network.result

/**
 * 给 `:core:mvvm` 里 `envelopedRequest` 链式请求配的码表。
 *
 * - [hubFirstBusinessCodes]：特码，链里用 `onHubFirstBusiness` 或交全局
 * - [encapsulationMessageToastCodes]：在表里、无 onError 回调、且**未** `skipAutoInfoToast` 时，用 `message` 发 info Toast
 * - 其他 code：不自动 Toast，靠 `onError` 或静默；整页错只由 `withAutoErrorPage` 开
 */
object RequestChainErrorDispatch {
    const val USER_VISIBLE_NETWORK_ERROR: String = "网络异常，请稍后重试"

    val hubFirstBusinessCodes: MutableSet<Int> = mutableSetOf(
        BusinessErrorCodes.UNAUTHORIZED_LOGOUT,
        1001,
    )

    /** 无 `onError` 时链上自动用 `message` 发 info；有壳链 `skipAutoInfoToast` 为 true 则不再自动。 */
    val encapsulationMessageToastCodes: MutableSet<Int> = mutableSetOf(
        -1,
        -2,
        1009,
    )

    fun isHubFirstBusinessCode(code: Int): Boolean = code in hubFirstBusinessCodes

    fun isEncapsulationMessageToastCode(code: Int): Boolean = code in encapsulationMessageToastCodes
}

package com.example.myapplication.common

/** CC 组件名（跨模块通信用字符串常量）。 */
object CcNames {
    const val GAME = "cc.game"

    @Deprecated("使用 GAME", ReplaceWith("CcNames.GAME"))
    const val HOME = GAME

    /** 与 [com.example.myapplication.navigation.RoutePaths.SOCIAL] 对应。 */
    const val SOCIAL = "cc.social"

    @Deprecated("使用 SOCIAL", ReplaceWith("CcNames.SOCIAL"))
    const val LIVE = SOCIAL

    /** 与 [com.example.myapplication.navigation.RoutePaths.MALL] 对应。 */
    const val MALL = "cc.mall"

    @Deprecated("使用 MALL", ReplaceWith("CcNames.MALL"))
    const val COMMERCE = MALL

    const val SANDBOX_BACKGROUND = "cc.sandbox.background"
    const val SANDBOX_MODULE_ADAPTER = "cc.sandbox.module_adapter"
    const val SANDBOX_PAGE_STATE = "cc.sandbox.page_state"
}

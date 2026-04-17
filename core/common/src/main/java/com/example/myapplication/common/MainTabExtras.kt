package com.example.myapplication.common

/** MainActivity 与 CC 跳转约定：通过 Intent / ARouter 携带 tab 参数。 */
object MainTabExtras {
    const val EXTRA_TAB = "main_tab"

    /** 游戏广场 Tab（与 Ludex `gamesFragment` 对应）。 */
    const val TAB_GAME = "game"

    /** 历史约定值 `"home"`，与 [TAB_GAME] 在门户层映射到同一 Tab。 */
    const val TAB_HOME = "home"

    const val TAB_SOCIAL = "social"

    @Deprecated("使用 TAB_SOCIAL", ReplaceWith("MainTabExtras.TAB_SOCIAL"))
    const val TAB_LIVE = TAB_SOCIAL

    const val TAB_MALL = "mall"

    @Deprecated("使用 TAB_MALL", ReplaceWith("MainTabExtras.TAB_MALL"))
    const val TAB_COMMERCE = TAB_MALL

    /** 个人 / 登录态占位（与 Ludex `profileFragment` / `authFragment` 能力对齐的壳内入口）。 */
    const val TAB_PROFILE = "profile"
}

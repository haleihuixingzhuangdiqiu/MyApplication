package com.example.myapplication.navigation

/**
 * ARouter 路径常量（跨模块唯一入口）；页面实现留在各 `feature:*`，此处仅字符串契约。
 *
 * 跨域跳转请使用本对象 + ARouter，勿在 `feature` 间 `implementation(project(":feature:其它业务"))` 或引用对方 `Activity` 类。
 */
object RoutePaths {
    /** 门户 Activity 由 Manifest 启动，一般不配 ARouter @Route（避免与 Hilt Java 编译冲突）。常量可保留给文档或其它跳转约定。 */
    const val MAIN = "/app/main"

    /** 账号密码登录（壳模块实现）。 */
    const val LOGIN = "/app/login"

    /** 首段需各模块唯一，否则多模块会生成同名 ARouter$$Group$$feature 导致 dex 冲突。 */
    /** Ludex 业务域：游戏广场（原 `feature:home` 演示链路迁入此模块）。 */
    const val GAME = "/game/main"

    /** 游戏广场帖子详情（与列表封面共享元素 Hero）。 */
    const val GAME_POST_DETAIL = "/game/post/detail"

    /** 关注流 / 关系维护（原 `live` 占位名与能力不一致，路由已迁移）。 */
    const val SOCIAL = "/social/main"

    /** 关注流动态详情（共享元素过渡由页面自行 ActivityOptionsCompat 触发）。 */
    const val SOCIAL_FEED_DETAIL = "/social/feed/detail"

    /** 好物广场 / 目录购物车（原 `commerce` 占位名与能力不一致，路由已迁移）。 */
    const val MALL = "/mall/main"

    /** 好物条目详情（与列表封面共享元素 Hero）。 */
    const val MALL_ITEM_DETAIL = "/mall/item/detail"

    const val SANDBOX_HUB = "/sandbox/hub"
    const val SANDBOX_BACKGROUND = "/sandbox/background"
    const val SANDBOX_MODULE_ADAPTER = "/sandbox/module_adapter"

    /** 全屏页面状态（Loading / Empty / Error）试页面。 */
    const val SANDBOX_PAGE_STATE = "/sandbox/page_state"

    /** MMKV 包装 + 375 宽适配联调页。 */
    const val SANDBOX_STORAGE_ADAPT = "/sandbox/storage_adapt"

    /** BaseViewModel 方法调用测试页。 */
    const val SANDBOX_VIEWMODEL_TEST = "/sandbox/viewmodel_test"
}

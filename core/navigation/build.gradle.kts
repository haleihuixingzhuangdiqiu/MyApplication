plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    id("com.example.myapplication.android.library")
}

android {
    namespace = "com.example.myapplication.navigation"
}

/**
 * 路由路径常量 [RoutePaths]、ARouter 轨迹 [RouteStack] / [RouteStackInterceptor]。
 */
dependencies {
    implementation(libs.arouter.api)
    kapt(libs.arouter.compiler)
}

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", "navigation")
    }
}

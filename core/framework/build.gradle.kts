plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.example.myapplication.android.library")
}

android {
    namespace = "com.example.myapplication.framework"
}

/** 路由轨迹、事件总线、RecyclerView/ModuleAdapter 扩展、Toolbar Ktx；UI 壳、Base 系、**网络链式请求**（[com.example.myapplication.mvvm.request]）在 :core:mvvm。 */
dependencies {
    implementation(project(":core:common"))
    api(project(":core:mvvm"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.module.adapter)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
}

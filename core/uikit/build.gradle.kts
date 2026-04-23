plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.example.myapplication.android.library")
}

android {
    namespace = "com.example.myapplication.uikit"
}

/**
 * 占位模块：后续可放纯 UI 组件。分页与 [com.example.myapplication.mvvm.paging.BasePagedViewModel] / [com.example.myapplication.mvvm.paging.RefreshListHost] 已迁至 :core:mvvm。
 */
dependencies {
    implementation(libs.androidx.core.ktx)
}

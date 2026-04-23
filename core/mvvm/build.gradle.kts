plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.example.myapplication.android.library")
}

android {
    namespace = "com.example.myapplication.mvvm"
}

/**
 * 无 UI 的 ViewModel 与状态模型；展示层（Activity/Fragment/overlay）见 :core:framework。
 */
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.example.myapplication.android.library")
}

android {
    namespace = "com.example.myapplication.mvvm"
    buildFeatures {
        dataBinding = true
    }
}

/**
 * ViewModel / 状态 / 协程封装，以及 **Base 系 Activity·Fragment**、DataBinding、整页蒙层 [PageOverlayHost]、AutoSize 策略；
 * **dataRequest** / **envelopedRequest** 链式网络封装（[com.example.myapplication.mvvm.request]）。
 * 路由轨迹、LiveDataBus、列表/工具条扩展见 :core:framework。
 *
 * 通用分页基类、SmartRefresh 绑定 [com.example.myapplication.mvvm.paging.RefreshListHost] 与可复用列表布局亦在本模块。
 */
dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(libs.toaster)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.google.material)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.autosize)

    api(libs.smartRefreshLayoutKernel)
    api(libs.smartRefreshLayoutHeaderClassics)
    api(libs.smartRefreshLayoutFooterClassics)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

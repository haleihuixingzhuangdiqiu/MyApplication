plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.example.myapplication.android.library")
}

android {
    namespace = "com.example.myapplication.network"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    api(libs.okhttp)
    api(libs.retrofit)
    api(libs.retrofit.converter.gson)
}

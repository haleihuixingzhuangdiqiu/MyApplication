plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.example.myapplication.android.library")
}

android {
    namespace = "com.example.myapplication.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation("javax.inject:javax.inject:1")
    compileOnly(libs.hilt.android)
}

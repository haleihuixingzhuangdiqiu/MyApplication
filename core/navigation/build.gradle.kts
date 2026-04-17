plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.example.myapplication.android.library")
}

android {
    namespace = "com.example.myapplication.navigation"
}

dependencies {}

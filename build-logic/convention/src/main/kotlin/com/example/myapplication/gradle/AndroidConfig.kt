package com.example.myapplication.gradle

import org.gradle.api.JavaVersion

/** 全局 Android 编译约定（min/target/compile SDK 的单一来源）。 */
internal object AndroidConfig {
    const val COMPILE_SDK_MAJOR = 36
    const val COMPILE_SDK_MINOR = 1

    const val MIN_SDK = 24
    const val TARGET_SDK = 36

    val JAVA_VERSION: JavaVersion = JavaVersion.VERSION_11

    const val TEST_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
}

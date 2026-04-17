package com.example.myapplication.gradle

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * `com.android.application` 模块的公共约定：SDK、Java、构建类型等。
 * 模块专属内容（namespace、applicationId、version）仍在各模块 `build.gradle.kts` 中声明。
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.withPlugin("com.android.application") {
            target.extensions.configure<ApplicationExtension> {
                compileSdk {
                    version = release(AndroidConfig.COMPILE_SDK_MAJOR) {
                        minorApiLevel = AndroidConfig.COMPILE_SDK_MINOR
                    }
                }

                defaultConfig {
                    minSdk = AndroidConfig.MIN_SDK
                    targetSdk = AndroidConfig.TARGET_SDK
                    testInstrumentationRunner = AndroidConfig.TEST_RUNNER
                }

                buildTypes {
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }
                }

                compileOptions {
                    sourceCompatibility = AndroidConfig.JAVA_VERSION
                    targetCompatibility = AndroidConfig.JAVA_VERSION
                }
            }
        }
        target.pluginManager.withPlugin("org.jetbrains.kotlin.android") {
            target.tasks.withType(KotlinCompile::class.java).configureEach {
                compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }
}

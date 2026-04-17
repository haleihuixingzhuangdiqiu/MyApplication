package com.example.myapplication.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/** `com.android.library` 模块的公共约定（与 Application 对齐的 SDK / Java）。 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.withPlugin("com.android.library") {
            target.extensions.configure<LibraryExtension> {
                compileSdk {
                    version = release(AndroidConfig.COMPILE_SDK_MAJOR) {
                        minorApiLevel = AndroidConfig.COMPILE_SDK_MINOR
                    }
                }
                defaultConfig {
                    minSdk = AndroidConfig.MIN_SDK
                    testInstrumentationRunner = AndroidConfig.TEST_RUNNER
                }
                buildTypes {
                    release {
                        isMinifyEnabled = false
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

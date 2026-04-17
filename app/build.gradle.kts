import org.gradle.api.tasks.compile.JavaCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    id("com.example.myapplication.android.application")
}

android {
    namespace = "com.example.myapplication"

    defaultConfig {
        applicationId = "com.example.myapplication"
        versionCode = 1
        versionName = "1.0"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["AROUTER_MODULE_NAME"] = "app"
            }
        }
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:navigation"))
    implementation(project(":core:framework"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":feature:game"))
    implementation(project(":feature:social"))
    implementation(project(":feature:mall"))
    implementation(project(":feature:sandbox"))

    implementation(libs.bundles.app.runtime)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.google.material)
    implementation(libs.background.library)
    implementation(libs.lottie)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.androidx.startup)
    implementation(libs.timber)
    // 编译期用 no-op 的 API，Debug 运行挂载真实 Chucker，Release 仅 no-op，避免 main 引用不到类
    compileOnly(libs.chucker.noop)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)

    implementation(libs.coil)

    implementation(libs.arouter.api)
    kapt(libs.arouter.compiler)
    implementation(libs.cc.core)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("AROUTER_MODULE_NAME", "app")
    }
}

// Hilt 的 Java 编译步骤会再次触发注解处理器，需为 ARouter 传入模块名（与 kapt 一致）。
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-AAROUTER_MODULE_NAME=app")
}

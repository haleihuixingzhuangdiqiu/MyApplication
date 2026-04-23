plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    id("com.example.myapplication.android.library")
}

android {
    namespace = "com.example.myapplication.sandbox"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:navigation"))
    implementation(project(":core:mvvm"))
    implementation(project(":core:framework"))
    implementation(project(":core:network"))
    implementation(libs.bundles.app.runtime)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.google.material)
    implementation(libs.background.library)
    implementation(libs.module.adapter)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.arouter.api)
    implementation(libs.cc.core)
    kapt(libs.arouter.compiler)
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
}

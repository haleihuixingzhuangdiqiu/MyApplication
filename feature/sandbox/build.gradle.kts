plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    id("com.example.myapplication.android.library")
}

android {
    namespace = "com.example.myapplication.sandbox"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:navigation"))
    implementation(project(":core:framework"))
    implementation(libs.bundles.app.runtime)
    implementation(libs.google.material)
    implementation(libs.background.library)
    implementation(libs.module.adapter)
    implementation(libs.arouter.api)
    implementation(libs.cc.core)
    kapt(libs.arouter.compiler)
}

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
}

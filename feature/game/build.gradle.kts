plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    id("com.example.myapplication.android.library")
}

android {
    namespace = "com.example.myapplication.game"
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:navigation"))
    implementation(project(":core:framework"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil)
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

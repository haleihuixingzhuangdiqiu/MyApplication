plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    id("com.example.myapplication.android.library")
}

android {
    namespace = "com.example.myapplication.framework"
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.module.adapter)
    implementation(libs.google.material)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx) // repeatOnLifecycle
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.arouter.api)
    kapt(libs.arouter.compiler)
}

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", "framework")
    }
}

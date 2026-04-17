plugins {
    `kotlin-dsl`
}

group = "com.example.myapplication.buildlogic"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
}

gradlePlugin {
    plugins {
        register("androidApplicationConvention") {
            id = "com.example.myapplication.android.application"
            implementationClass = "com.example.myapplication.gradle.AndroidApplicationConventionPlugin"
        }
        register("androidLibraryConvention") {
            id = "com.example.myapplication.android.library"
            implementationClass = "com.example.myapplication.gradle.AndroidLibraryConventionPlugin"
        }
    }
}

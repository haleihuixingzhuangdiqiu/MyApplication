pluginManagement {
    includeBuild("build-logic")
    plugins {
        id("org.jetbrains.kotlin.android") version "2.0.21" apply false
        id("org.jetbrains.kotlin.kapt") version "2.0.21" apply false
        id("com.android.library") version "9.0.1" apply false
        id("com.google.dagger.hilt.android") version "2.57.2" apply false
    }
    repositories {
        maven(url = "https://maven.aliyun.com/repository/google")
        maven(url = "https://maven.aliyun.com/repository/public")
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Gradle 9 同一 catalog 只能 from 一次：将 versions.toml + libraries.toml 合并后再交给 catalog
val mergedLibsVersionCatalog = run {
    val out = layout.rootDirectory.file("build/tmp/libs.version-catalog.merged.toml").asFile
    out.parentFile.mkdirs()
    val versions = layout.rootDirectory.file("version-catalog/versions.toml").asFile.readText()
    val libraries = layout.rootDirectory.file("version-catalog/libraries.toml").asFile.readText()
    out.writeText("$versions\n\n$libraries")
    out
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = "https://maven.aliyun.com/repository/google")
        maven(url = "https://maven.aliyun.com/repository/public")
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            from(files(mergedLibsVersionCatalog))
        }
    }
}

rootProject.name = "My Application"
include(":app")
include(":core:common")
include(":core:mvvm")
include(":core:navigation")
include(":core:framework")
include(":core:database")
include(":core:network")
include(":feature:game")
include(":feature:social")
include(":feature:mall")
include(":feature:sandbox")

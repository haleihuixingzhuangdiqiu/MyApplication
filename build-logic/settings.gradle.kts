pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// 与主工程共用 ../version-catalog/versions.toml + libraries.toml，合并规则与根 settings 一致
val mergedLibsVersionCatalog = run {
    val projectRoot = settings.rootDir.parentFile
    val out = projectRoot.resolve("build/tmp/libs.version-catalog.merged.toml")
    out.parentFile.mkdirs()
    out.writeText(
        projectRoot.resolve("version-catalog/versions.toml").readText() + "\n\n" +
            projectRoot.resolve("version-catalog/libraries.toml").readText(),
    )
    out
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files(mergedLibsVersionCatalog))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")

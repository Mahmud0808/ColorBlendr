pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
        maven {
            setUrl("https://jitpack.io")
        }
    }
    versionCatalogs {
        create("libs")
    }
}

rootProject.name = "ColorBlendr"

include(":app")
include(":systemstubs")
include(":libadb")
include(":colorpickerdialog")
include(":materialcolorutilities")

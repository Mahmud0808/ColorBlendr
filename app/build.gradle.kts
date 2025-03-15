import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.drdisagree.colorblendr"
    compileSdk = 35

    defaultConfig {
        minSdk = 31
        targetSdk = 35
        versionCode = 26
        versionName = "v1.11.5"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            applicationVariants.all {
                val variant = this
                variant.outputs
                    .map { it as BaseVariantOutputImpl }
                    .forEach { output ->
                        val outputFileName = "ColorBlendr ${variant.versionName}.apk"
                        output.outputFileName = outputFileName
                    }
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        aidl = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    compileOnly(project(":systemstubs"))

    implementation(libs.core.ktx)
    implementation(libs.libsu.core)
    implementation(libs.libsu.service)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.lsposed.hiddenapibypass)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    implementation(libs.remotepreferences)
    implementation(libs.circleimageview)
    implementation(libs.glide)
    implementation(libs.preference.ktx)
    implementation(libs.androidUtils)
    implementation(libs.core.splashscreen)
    implementation(libs.work.runtime)
    implementation(libs.palette)
    implementation(libs.flexbox)
    implementation(libs.gson)
    implementation(libs.recyclerview)
    implementation(libs.recyclerview.selection)
    implementation(libs.blurView)
    implementation(libs.lifecycle.common.jvm)
    ksp(libs.glide.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.zip4j)
}
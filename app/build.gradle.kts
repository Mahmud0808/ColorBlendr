import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin.ksp)
}

configure<ApplicationExtension> {
    namespace = "com.drdisagree.colorblendr"
    compileSdk = 36

    defaultConfig {
        minSdk = 31
        targetSdk = 36
        versionCode = 40
        versionName = "v2.1.1"

        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64"))
        }
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    var releaseSigning = signingConfigs.getByName("debug")

    try {
        val keystoreProperties = Properties()
        FileInputStream(keystorePropertiesFile).use { inputStream ->
            keystoreProperties.load(inputStream)
        }

        releaseSigning = signingConfigs.create("release") {
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
        }
    } catch (_: Exception) {
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = releaseSigning

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = releaseSigning

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
            excludes += setOf(
                "/META-INF/*",
                "/META-INF/versions/**",
                "/org/bouncycastle/**",
                "/kotlin/**",
                "/kotlinx/**"
            )
        }
        resources.excludes += setOf(
            "/META-INF/*",
            "/META-INF/versions/**",
            "/org/bouncycastle/**",
            "/kotlin/**",
            "/kotlinx/**",
            "rebel.xml",
            "/*.txt",
            "/*.bin",
            "/*.json"
        )
    }
}

base {
    archivesName = "ColorBlendr ${android.defaultConfig.versionName}"
}

tasks.withType<KotlinCompile>().configureEach {
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_3
        jvmTarget = JvmTarget.JVM_17
    }
}

tasks.register("renameReleaseApk") {
    dependsOn("assembleRelease")

    doLast {
        val apkDir = layout.buildDirectory.dir("outputs/apk/release").get().asFile
        val originalApk = apkDir.listFiles()
            ?.firstOrNull { it.name.endsWith(".apk") }
            ?: throw GradleException("Release APK not found")

        val newName = "ColorBlendr-${android.defaultConfig.versionName}.apk"
        val renamedApk = File(apkDir, newName)

        originalApk.renameTo(renamedApk)
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            if (output is VariantOutputImpl) {
                output.outputFileName = "ColorBlendr ${android.defaultConfig.versionName}.apk"
            }
        }
    }
}

dependencies {
    compileOnly(project(":systemstubs"))
    implementation(project(":libadb"))
    implementation(project(":colorpickerdialog"))
    implementation(project(":materialcolorutilities"))

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
    implementation(libs.sun.security.android)
}
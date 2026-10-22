import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.agp.library)
}

abstract class GenerateMcuSources : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val upstreamDir: DirectoryProperty

    @get:Input
    abstract val packagePrefix: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val root = upstreamDir.get().asFile
        val prefix = packagePrefix.get()
        val out = outputDir.get().asFile.apply { deleteRecursively() }
        val packages = root.listFiles { file -> file.isDirectory }.orEmpty().joinToString("|") { it.name }
        val rewrite = Regex("^(package|import)(\\s+)($packages)\\b", RegexOption.MULTILINE)

        root.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
            val target = out.resolve(prefix.replace('.', '/')).resolve(file.relativeTo(root).path)
            target.parentFile.mkdirs()
            target.writeText(
                rewrite.replace(file.readText()) {
                    "${it.groupValues[1]}${it.groupValues[2]}$prefix.${it.groupValues[3]}"
                }
            )
        }
    }
}

androidComponents {
    onVariants { variant ->
        val task = tasks.register<GenerateMcuSources>(
            "generate${variant.name.replaceFirstChar(Char::titlecase)}McuSources"
        ) {
            upstreamDir.set(layout.projectDirectory.dir("upstream/kotlin"))
            packagePrefix.set("com.drdisagree.materialcolorutilities")
        }
        variant.sources.kotlin?.addGeneratedSourceDirectory(task, GenerateMcuSources::outputDir)
    }
}

android {
    namespace = "com.drdisagree.materialcolorutilities"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 31
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
}
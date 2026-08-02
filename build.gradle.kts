plugins {
    alias(libs.plugins.agp.app) apply false
    alias(libs.plugins.agp.library) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

tasks.register("Delete", Delete::class) {
    description = "Delete build directory"
    delete(project.layout.buildDirectory)
}
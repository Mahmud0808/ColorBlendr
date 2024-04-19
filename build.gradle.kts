tasks.register("Delete", Delete::class) {
    delete(project.layout.buildDirectory)
}
plugins {
    id("com.github.node-gradle.node") version "7.1.0"
    id("base") // Fügt die 'clean'-Task hinzu
}

node {
    version.set("16.15.0")
    npmVersion.set("8.5.4")
    download.set(true)
    workDir.set(layout.projectDirectory.dir("node/nodejs"))
    npmWorkDir.set(layout.projectDirectory.dir("node/npm"))
    nodeProjectDir.set(layout.projectDirectory.dir("."))
}

tasks.named<Delete>("clean") {
    delete(
        file("node"),
        file("node_modules"),
        layout.buildDirectory
    )
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("npmBuild") {
    group = "build"
    description = "Builds the React project"
    args.set(listOf("run", "build"))
    dependsOn("npmInstall")

    // Definiere explizit `build/webapp` als Output
    outputs.dir(layout.buildDirectory.dir("webapp"))

    doLast {
        val webappDir = layout.buildDirectory.dir("webapp").get().asFile
        if (!webappDir.exists()) {
            webappDir.mkdirs() // Erstelle das Verzeichnis, falls es nicht existiert
        }
    }
}

tasks.register<Jar>("webAppJar") {
    group = "build"
    description = "Package React build output as a JAR"
    archiveBaseName.set("borgbutler-webapp")
    archiveVersion.set(project.version.toString())
    destinationDirectory.set(layout.buildDirectory.dir("libs"))

    dependsOn("npmBuild")

    // Korrekte Referenz auf `webapp`-Verzeichnis
    inputs.dir(layout.buildDirectory.dir("webapp"))
    outputs.file(archiveFile)

    from(layout.buildDirectory.dir("webapp")) {
        into("static")
        exclude("resources", "libs", "tmp")
    }

    from(layout.projectDirectory.file("src/index.html")) {
        into("static")
    }
}

tasks.named("build") {
    dependsOn("webAppJar")
}

description = "borgbutler-webapp"

description = "borgbutler-webapp"

tasks.register<Exec>("npmBuild") {
    workingDir = projectDir
    commandLine("sh", "-c", "npm run build")
}

tasks.register<Zip>("packageWebApp") {
    outputs.upToDateWhen { false } // Immer ausführen, damit nichts fehlt
    dependsOn("npmBuild")

    archiveBaseName.set("borgbutler-webapp")
    archiveExtension.set("jar")
    destinationDirectory.set(projectDir)

    from("build") {
        into("webapp") // Statische Ressourcen in webapp-Verzeichnis
    }

    doLast {
        val jarFile = archiveFile.get().asFile
        val targetDir = buildDir.resolve("libs")

        mkdir(targetDir)
        ant.invokeMethod("move", mapOf("file" to jarFile.absolutePath, "todir" to targetDir.absolutePath))

        println("*** packageWebApp finished.")
    }
}

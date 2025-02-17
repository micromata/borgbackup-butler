import org.gradle.api.tasks.bundling.Zip
import java.util.Properties

plugins {
    java
    application
    distribution
    id("org.springframework.boot") version "3.4.2" // Füge das Spring Boot Plugin hinzu
    id("io.spring.dependency-management") version "1.1.4" // Dependency Management Plugin für Spring Boot
}

description = "borgbutler-server"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":borgbutler-core"))
    implementation(project(":borgbutler-webapp"))
    implementation("org.apache.commons:commons-text:1.13.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.commons:commons-compress:1.27.1")
    implementation("commons-io:commons-io:2.18.0")
    implementation("org.springframework:spring-core:6.2.3")

    implementation("org.springframework.boot:spring-boot-starter-web:3.4.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.2")
    implementation("commons-cli:commons-cli:1.9.0")
    testImplementation("org.mockito:mockito-core:5.15.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

sourceSets {
    main {
        java.srcDirs("src/main/java", "src/main/kotlin")
    }
}

application {
    mainClass.set("de.micromata.borgbutler.server.BorgButlerApplication")
}

tasks.register("createVersionProperties") {
    dependsOn(tasks.processResources)
    doLast {
        val versionFile = file("$buildDir/resources/main/version.properties")
        val properties = Properties().apply {
            setProperty("version", project.version.toString())
            setProperty("name", project.name)
            setProperty("build.date.millis", System.currentTimeMillis().toString())
        }
        versionFile.writer().use { properties.store(it, null) }
    }
}

tasks.classes {
    dependsOn(tasks.named("createVersionProperties"))
}

tasks.named("distZip", Zip::class) {
    dependsOn(":borgbutler-webapp:webAppJar")
}

tasks.register("dist") {
    dependsOn(tasks.named("distZip"))
}

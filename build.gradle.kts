import org.gradle.api.JavaVersion
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    kotlin("jvm") version "1.8.22" apply false
}

allprojects {
    group = "de.micromata.borgbutler"
    version = "0.8"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:unchecked")
        options.isDeprecation = true
        options.encoding = "UTF-8"
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter-api:5.11.4")
        add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine:5.11.4")
        add("testRuntimeOnly", "org.junit.vintage:junit-vintage-engine:5.11.4")
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}

// Gradle Wrapper Task für Version 8.0
tasks.named<Wrapper>("wrapper") {
    gradleVersion = "8.0"
}

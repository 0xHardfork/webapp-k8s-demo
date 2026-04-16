plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.10"
    id("org.springframework.boot") version "4.0.0-M1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.night"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.micrometer:micrometer-registry-prometheus")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Exec>("buildImage") {
    group = "build"
    description = "Build a containerd image using colima nerd"

    val imageName = "${project.name}:${project.version}"

    // Gradle daemon does not always inherit the full PATH (e.g. /opt/homebrew/bin)
    val colimaExecutable = listOf("/opt/homebrew/bin/colima", "/usr/local/bin/colima", "colima")
        .firstOrNull { File(it).exists() } ?: "colima"

    // The Dockerfile executes the Gradle build internally (multi-stage build), Therefore it does not depend on bootJar here directly.
    // Use '--' before nerdctl arguments so colima doesn't try to parse them as its own flags
    commandLine(colimaExecutable, "nerd", "--", "build", "-t", imageName, ".")
}

tasks.register<Exec>("saveImage") {
    group = "build"
    description = "Save the generated containerd image to a local .tar file"
    dependsOn("buildImage")

    val imageName = "${project.name}:${project.version}"
    val tarFile = layout.buildDirectory.file("${project.name}-${project.version}.tar").get().asFile

    val colimaExecutable = listOf("/opt/homebrew/bin/colima", "/usr/local/bin/colima", "colima")
        .firstOrNull { File(it).exists() } ?: "colima"

    doFirst {
        println("Saving image to ${tarFile.absolutePath} ...")
    }

    commandLine(colimaExecutable, "nerd", "--", "image", "save", "-o", tarFile.absolutePath, imageName)

    doLast {
        println("✅ Image successfully saved to: ${tarFile.absolutePath}")
    }
}


import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    application
}

group = "io.github.ufukhalis"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.arrow-kt:arrow-core:1.1.2")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.1.2")

    implementation("io.ktor:ktor-client-core:2.1.2")
    implementation("io.ktor:ktor-client-cio:2.1.2")
    implementation("io.ktor:ktor-client-content-negotiation:2.1.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.2")

    implementation ("io.insert-koin:koin-core:3.2.2")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.0")
    implementation("ch.qos.logback:logback-classic:1.4.3")

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")

    testImplementation("io.mockk:mockk:1.13.2")
    testImplementation("io.insert-koin:koin-test:3.2.2")
    testImplementation("io.kotest:kotest-runner-junit5:5.5.0")
    testImplementation("io.kotest.extensions:kotest-extensions-koin:1.1.0")
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

application {
    mainClass.set("io.github.ufukhalis.ind.MainKt")
}

val jar by tasks.getting(Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.WARN
    manifest {
        attributes["Main-Class"] = "io.github.ufukhalis.ind.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

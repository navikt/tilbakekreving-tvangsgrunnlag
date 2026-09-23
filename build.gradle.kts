
plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    id("io.ktor.plugin") version "3.5.2"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}
val logbackVersion = "1.6.1"

group = "no.nav"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(25)
}
dependencies {
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.swagger)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.metrics.micrometer)
    implementation(ktorLibs.server.requestValidation)
    implementation(ktorLibs.server.statusPages)
    implementation("io.micrometer:micrometer-registry-prometheus:1.17.1")
    implementation("io.opentelemetry.instrumentation:opentelemetry-ktor-3.0:2.18.1-alpha")
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:1.62.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}

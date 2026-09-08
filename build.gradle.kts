
plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    id("io.ktor.plugin") version "3.5.2"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}
val logbackVersion = "1.6.1"
val tokenValidationVersion = "5.0.30"
val mockOauth2ServerVersion = "6.0.2"

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
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("no.nav.security:token-validation-ktor-v3:$tokenValidationVersion")
    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
    testImplementation("no.nav.security:mock-oauth2-server:$mockOauth2ServerVersion")
}

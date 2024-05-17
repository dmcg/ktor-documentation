import org.gradle.plugins.ide.idea.model.IdeaModel

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
    id("idea")
}


application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation(platform("org.http4k:http4k-bom:5.18.2.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-format-kotlinx-serialization")
    implementation("org.http4k:http4k-server-netty")
    implementation("org.http4k:http4k-server-jetty")
    implementation("org.http4k:http4k-client-okhttp")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("org.http4k:http4k-testing-strikt")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}

configure<IdeaModel> {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
import org.gradle.kotlin.dsl.*
import java.util.*

buildscript {
    val buildSnapshotTrain: String by project
    val kotlinSnapshotVersion: String? by project
    val kotlinVersion: String by project

    repositories {
        mavenCentral()
        google()
    }

    if (buildSnapshotTrain == "true") {
        requireNotNull(kotlinSnapshotVersion) { "'kotlinSnapshotVersion' should be defined when building with snapshot compiler" }

        repositories {
            mavenLocal()
            maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        }

        configurations.configureEach {
            resolutionStrategy.eachDependency {
                if (requested.group == "org.jetbrains.kotlin") {
                    useVersion(kotlinVersion)
                }
            }
        }
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

fun check(version: Any, libVersion: String, libName: String) {
    if (version != libVersion) {
        throw IllegalStateException("Current deploy version is $version, but $libName version is not overridden ($libVersion)")
    }
}

allprojects {
    val buildSnapshotTrain: String by project

    repositories {
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev") }
    }

    if (buildSnapshotTrain == "true") {
        val kotlinVersion: String by project
        println("Using Kotlin $kotlinVersion for project $this")
        val deployVersion: Any? by project
        version = deployVersion ?: version

        val skipSnapshotChecks: Boolean by project

        if (!skipSnapshotChecks) {
            check(version, project.extra["atomicfuVersion"].toString(), "atomicfu")
            check(version, project.extra["kotlinxCoroutinesVersion"].toString(), "coroutines")
            check(version, project.extra["kotlinxSerializationVersion"].toString(), "serialization")
            check(version, project.extra["ktorVersion"].toString(), "ktor")
        }

        repositories {
            mavenLocal()
            maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        }
    }
}

val ktorRepositoryDir = file("${layout.buildDirectory}/m2")

if (ktorRepositoryDir.exists()) {
    allprojects {
        repositories {
            maven { url = uri(ktorRepositoryDir.absolutePath) }
        }
    }
} else {
    allprojects {
        repositories {
            mavenLocal()
        }
    }
}

val mavenSettingsFile = file("settings.xml")

subprojects {
    val kotlinVersion: String by project
    val ktorVersion: String by project

    if (name.startsWith("maven-")) {
        val mvnw = if (System.getProperty("os.name").lowercase(Locale.US).contains("windows")) {
            listOf("cmd", "/c", "mvnw.cmd")
        } else {
            listOf("./mvnw")
        }

        var mavenCmd = mvnw + listOf(
            "-Dkotlin.version=$kotlinVersion",
            "-Dktor.version=$ktorVersion"
        )

        if (ktorRepositoryDir.exists()) {
            mavenCmd += listOf("-Dktor.repository.url=file:///${ktorRepositoryDir.absolutePath}", "-s", mavenSettingsFile.absolutePath)
        }

        tasks.register("build", Exec::class) {
            commandLine = mavenCmd + listOf("package")
        }

        tasks.register("clean", Exec::class) {
            commandLine = mavenCmd + listOf("clean")
        }

        println("Starting maven with command: $mavenCmd")

        if (name.contains("appengine")) {
            tasks.register("run", Exec::class) {
                commandLine = mavenCmd + listOf("appengine:run")
            }
        } else {
            tasks.register("run", Exec::class) {
                commandLine = mavenCmd + listOf("compile", "exec:java")
            }
        }
    }
}

if (project.extra["buildSnapshotTrain"] == "true") {
    println("Hacking test tasks, removing stress and flaky tests")
    allprojects {
        tasks.withType<Test>().configureEach {
            // Add: exclude("**/*TestName*") here
        }
    }

    println("Manifest of kotlin-compiler-embeddable.jar for coroutines")
    subprojects.find { it.name == "kotlinx-coroutines-core" }?.let { coroutinesProject ->
        coroutinesProject.configurations
            .matching { it.name == "kotlinCompilerClasspath" }
            .configureEach {
                resolvedConfiguration.files.find {
                    it.name.contains("kotlin-compiler-embeddable")
                }?.let { file ->
                    zipTree(file).matching {
                        include("META-INF/MANIFEST.MF")
                    }.singleFile.readLines().forEach(::println)
                }
            }
    }
}

subprojects {
    tasks.withType<Test>().configureEach {
        systemProperty("gradlew", file("../../gradlew").absolutePath)
    }
}
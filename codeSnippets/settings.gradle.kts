pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.google.cloud.tools.appengine")) {
                useModule("com.google.cloud.tools:appengine-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "ktor-codesnippets"

fun module(group: String, name: String) {
    include(name)
    project(":$name").projectDir = file("$group/$name")
}

// ---------------------------


module("snippets", "tutorial-http-api")

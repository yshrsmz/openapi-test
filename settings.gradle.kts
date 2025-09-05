// The settings file is the entry point of every Gradle build.
// Its primary purpose is to define the subprojects.
// It is also used for some aspects of project-wide configuration, like managing plugins, dependencies, etc.
// https://docs.gradle.org/current/userguide/settings_file_basics.html

// Configure plugin management
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
//        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
//        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/")}
    }
}

// Include the runtime and gradle plugin modules in the build.
// If there are changes in only one of the modules, Gradle will rebuild only the one that has changed.
// Learn more about structuring projects with Gradle - https://docs.gradle.org/8.7/userguide/multi_project_builds.html
include(":openapikotlin-runtime")
include(":openapikotlin-gradle-plugin")
include(":example-simple-api")
include(":example-petstore")
include(":example-ory-client")

include(":openapi-gen")
include(":openapi-gen:multiplatform")
include(":openapi-gen:ktor")
include(":openapi-gen:retrofit")

rootProject.name = "openapi-test"

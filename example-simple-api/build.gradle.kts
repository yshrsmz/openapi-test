buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("com.codingfeline.openapi") version "1.0.0-SNAPSHOT"
    alias(libs.plugins.kotlinx.serialization)
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.codingfeline.openapikotlin:openapikotlin-runtime:1.0.0-SNAPSHOT")
    
    // Ktor client engine for runtime
    implementation("io.ktor:ktor-client-cio:3.2.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.2.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.2")
    
    // Logging - using nop to avoid warnings
    implementation("org.slf4j:slf4j-nop:2.0.9")
}

// OpenAPI plugin configuration
openApiKotlin {
    inputSpec = file("../openapi/simple-test.yaml")
    outputDir = layout.buildDirectory.file("generated/openapi").get().asFile
    packageName = "com.example.simple"
    
    models {
        generateDataAnnotations = true
        generateDefaultValues = true
    }
    
    client {
        clientClassName = "SimpleApiClient"
        generateErrorHandling = true
        generateAuthHelpers = false
    }
}

// Ensure code generation runs before compilation
tasks.compileKotlin {
    dependsOn("generateOpenApiCode")
}

// Configure main class
application {
    mainClass.set("com.example.simple.MainKt")
}
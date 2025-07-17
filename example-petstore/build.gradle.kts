buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("com.codingfeline.openapi") version "1.0.0-SNAPSHOT"
    kotlin("plugin.serialization") version "2.1.21"
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
    implementation("io.ktor:ktor-client-logging:3.2.2")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")
}

// OpenAPI plugin configuration
openApiKotlin {
    inputSpec = file("../openapi/petstore.json")
    outputDir = layout.buildDirectory.file("generated/openapi").get().asFile
    packageName = "com.example.petstore"
    
    models {
        generateDataAnnotations = true
        generateDefaultValues = true
    }
    
    client {
        clientClassName = "PetstoreApiClient"
        generateErrorHandling = true
        generateAuthHelpers = true
    }
}

// Ensure code generation runs before compilation
tasks.compileKotlin {
    dependsOn("generateOpenApiCode")
}

// Application plugin configuration
application {
    mainClass.set("com.example.petstore.PetstoreExampleKt")
}
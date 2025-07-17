buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("com.codingfeline.openapi") version "1.0.0-SNAPSHOT"
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
    
    // For testing generated code
    testImplementation("io.ktor:ktor-client-mock:3.2.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("com.google.truth:truth:1.4.4")
}

// OpenAPI plugin configuration
openApiKotlin {
    // Use the Ory API spec as an example
    inputSpec = file("../openapi/ory-client-1.20.22.json")
    
    // Output directory for generated code
    outputDir = layout.buildDirectory.file("generated/openapi").get().asFile
    
    // Package name for generated code
    packageName = "com.example.api.ory"
    
    // Model generation options
    models {
        generateDataAnnotations = true
        generateDefaultValues = true
    }
    
    // Client generation options
    client {
        clientClassName = "OryApiClient"
        generateErrorHandling = true
        generateAuthHelpers = true
    }
    
    // Validation options
    validation {
        failOnWarnings = false
        strict = true
    }
}

// Ensure code generation runs before compilation
tasks.compileKotlin {
    dependsOn("generateOpenApiCode")
}

// Application plugin configuration
application {
    mainClass.set("com.example.api.Example")
}

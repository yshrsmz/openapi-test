plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("com.codingfeline.openapi") version "1.0.0"
}

dependencies {
    implementation(project(":openapikotlin-runtime"))
    
    // Ktor client engine for runtime
    implementation("io.ktor:ktor-client-cio:3.2.2")
    
    // For testing generated code
    testImplementation("io.ktor:ktor-client-mock:3.2.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

// OpenAPI plugin configuration
openApi {
    // Use the Ory API spec as an example
    inputSpec = file("../openapi/ory-client-1.20.22.json")
    
    // Output directory for generated code
    outputDir = file("$buildDir/generated/openapi")
    
    // Package name for generated code
    packageName = "com.example.api.ory"
    
    // Model generation options
    models {
        generateDataAnnotations = true
        generateDefaultValues = true
    }
    
    // Client generation options
    client {
        generateClient = true
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

// Make generated sources available to the main source set
kotlin {
    sourceSets {
        main {
            kotlin.srcDir("$buildDir/generated/openapi")
        }
    }
}

// Ensure code generation runs before compilation
tasks.compileKotlin {
    dependsOn("generateOpenApiCode")
}
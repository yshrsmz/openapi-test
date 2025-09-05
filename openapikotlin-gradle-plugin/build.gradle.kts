plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    
    // Gradle plugin development
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.2.0"
    `maven-publish`
}

dependencies {
    // Gradle API
    implementation(gradleApi())
    
    // Kotlin Gradle Plugin API
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.21")
    
    // OpenAPI parsing
    implementation("io.swagger.parser.v3:swagger-parser:2.1.31")
    
    // Code generation
    implementation("com.squareup:kotlinpoet:2.2.0")
    
    // JSON parsing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    
    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.4")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        register("openApiKotlinGradlePlugin") {
            id = "com.codingfeline.openapi"
            implementationClass = "com.codingfeline.openapikotlin.gradle.OpenApiKotlinPlugin"
            displayName = "OpenAPI Kotlin Code Generator"
            description = "Generates Kotlin code from OpenAPI specifications"
        }
    }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

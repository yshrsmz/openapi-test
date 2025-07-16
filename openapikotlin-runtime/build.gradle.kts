plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
}

dependencies {
    // Ktor client dependencies
    api("io.ktor:ktor-client-core:3.2.2")
    api("io.ktor:ktor-client-cio:3.2.2")
    api("io.ktor:ktor-client-auth:3.2.2")
    api("io.ktor:ktor-client-content-negotiation:3.2.2")
    api("io.ktor:ktor-client-logging:3.2.2")
    api("io.ktor:ktor-serialization-kotlinx-json:3.2.2")
    
    // Kotlinx dependencies
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    
    // Kotlin reflection
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    // Test dependencies
    testImplementation("io.ktor:ktor-client-mock:3.2.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.4")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("com.google.truth:truth:1.4.4")
}
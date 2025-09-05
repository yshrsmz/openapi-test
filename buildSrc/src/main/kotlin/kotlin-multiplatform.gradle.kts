package buildsrc.convention

plugins {
    kotlin("multiplatform")
}

kotlin {
    // Use a specific Java version to make it easier to work in different environments.
    jvmToolchain(17)
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.openapi) apply false
//    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
//    alias(libs.plugins.jetbrains.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
}

allprojects {
    group = "com.codingfeline.openapikotlin"
    version = "1.0.0-SNAPSHOT"
}
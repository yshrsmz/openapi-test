# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin-based Gradle project focused on OpenAPI functionality. The project uses a multi-module structure with shared build logic extracted to convention plugins in `buildSrc`.

## Key Build Commands

- `./gradlew run` - Build and run the application
- `./gradlew build` - Build the application
- `./gradlew check` - Run all checks including tests
- `./gradlew clean` - Clean all build outputs
- `./gradlew test` - Run tests
- `./gradlew test --tests "TestClassName"` - Run a specific test class
- `./gradlew test --tests "TestClassName.testMethodName"` - Run a specific test method

## Project Architecture

### Module Structure
- **openapikotlin-gradle-plugin**: Main Gradle plugin module for OpenAPI Kotlin code generation
  - Main class: `com.codingfeline.openapikotlin.app.AppKt`
  - Plugin implementation and OpenAPI processing logic

### Build Configuration
- **buildSrc**: Contains convention plugins for shared build logic
  - `kotlin-jvm.gradle.kts`: Configures Kotlin JVM settings, Java 17 toolchain, and JUnit test configuration
- **gradle/libs.versions.toml**: Version catalog centralizing dependency management
  - Kotlin 2.1.21
  - Kotlinx libraries (coroutines, serialization, datetime)
- Build and configuration caching enabled for faster builds

### Testing
- Tests use JUnit Platform
- Test logging configured to show passed, failed, and skipped tests
- Located in `src/test/kotlin` directories within each module
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin-based Gradle plugin that generates type-safe Kotlin code from OpenAPI specifications. It follows clean architecture principles and uses KotlinPoet for code generation. The project uses a multi-module structure with shared build logic extracted to convention plugins in `buildSrc`.

## Current Status

- **42 tests**: 41 passing, 1 skipped
- Successfully generates code for complex real-world APIs (Petstore, Ory)
- All major features implemented except "separate clients by tags"

## Key Build Commands

- `./gradlew build` - Build the entire project
- `./gradlew check` - Run all checks including tests
- `./gradlew clean` - Clean all build outputs
- `./gradlew test` - Run all tests
- `./gradlew test --tests "TestClassName"` - Run a specific test class
- `./gradlew test --tests "TestClassName.testMethodName"` - Run a specific test method
- `./gradlew publishToMavenLocal` - Publish plugin to local Maven repository

### Example Module Commands
- `cd example && ../gradlew build` - Build the example module
- `cd example && ../gradlew generateOpenApiCode` - Generate code from OpenAPI spec

## Project Architecture

### Module Structure
- **openapikotlin-gradle-plugin**: Main Gradle plugin module for OpenAPI Kotlin code generation
  - Plugin ID: `com.codingfeline.openapi`
  - Clean architecture with domain, application, and infrastructure layers
  - Uses KotlinPoet for code generation
- **example**: Example project demonstrating plugin usage
  - Uses Ory API spec as example
  - Shows configuration options
- **openapi**: Contains test OpenAPI specifications

### Build Configuration
- **buildSrc**: Contains convention plugins for shared build logic
  - `kotlin-jvm.gradle.kts`: Configures Kotlin JVM settings, Java 17 toolchain, and JUnit test configuration
- **gradle/libs.versions.toml**: Version catalog centralizing dependency management
  - Kotlin 2.1.21
  - KotlinPoet 2.2.0
  - Swagger Parser 2.1.31
  - Ktor (for generated code)
  - kotlinx.serialization 1.9.0
  - kotlinx.datetime 0.7.0
- Build and configuration caching enabled for faster builds

### Testing
- Tests use JUnit Platform
- Test logging configured to show passed, failed, and skipped tests
- Located in `src/test/kotlin` directories within each module
- TDD methodology used throughout development

## Features Implemented

### Model Generation
- Data classes with proper nullability
- Enum generation (including nested enums)
- kotlinx.serialization support
- Property name sanitization
- Schema composition (allOf, oneOf, anyOf)

### Client Generation
- Ktor-based HTTP clients
- All HTTP methods supported
- Parameter handling (path, query, header)
- OAuth2 authentication helpers
- Type-safe responses

### Special Features
- Special types for undefined types (NullUUID, Time, etc.)
- Smart import management
- Comprehensive type mapping
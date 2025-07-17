# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin-based Gradle plugin that generates type-safe Kotlin code from OpenAPI specifications. It follows clean architecture principles and uses KotlinPoet for code generation. The project uses a multi-module structure with shared build logic extracted to convention plugins in `buildSrc`.

## Important Notes on Dynamic Types

When working with OpenAPI specs that have schemas without type definitions (like Ory's `identityTraits`), the plugin supports dynamic type handling:

- **Default behavior**: Maps to `Any` with warnings (will fail at runtime with kotlinx.serialization)
- **Recommended**: Enable `useJsonElementForDynamicTypes = true` in ModelsConfig
- **Smart inference**: Automatically uses JsonObject for object-like schemas, JsonArray for arrays
- **Override support**: Use `schemaTypeOverrides` map for specific schemas

## Current Status

- **51 tests**: 50 passing, 1 skipped
- Successfully generates code for complex real-world APIs (Petstore, Ory)
- All major features implemented except "separate clients by tags"
- Dynamic schema handling implemented with JsonElement support

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
- **openapikotlin-runtime**: Runtime library with OAuth2 client and serializers
- **example-simple-api**: Simple example with dynamic Config type
- **example-petstore**: Classic Swagger Petstore example
- **example-ory-client**: Complex example with 400+ models including dynamic types
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
- Dynamic schema handling with JsonElement/JsonObject/JsonArray
- Configurable behavior for untyped schemas (ALLOW/WARN/FAIL)
- Schema type overrides for fine-grained control

## Key Files for Dynamic Schema Handling

- **KotlinPoetTypeMapper.kt**: Core type mapping logic with dynamic type support
- **OpenApiKotlinExtension.kt**: Configuration classes including ModelsConfig
- **KotlinType.kt**: Domain value object with JsonElement/JsonObject/JsonArray types
- **DynamicSchemaHandlingTest.kt**: Comprehensive test suite for the feature
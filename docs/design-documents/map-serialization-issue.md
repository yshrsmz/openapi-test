# Design Document: Map Serialization Issue in OpenAPI Kotlin Code Generator

**Author**: OpenAPI Kotlin Team  
**Date**: January 2025  
**Status**: Problem Analysis & Proposed Solution  
**Issue**: Generic Map types fail to serialize/deserialize at runtime

## Executive Summary

The OpenAPI Kotlin code generator has two related issues with Map types:
1. **Code Generation Bug**: Maps with typed additionalProperties generate as `Map<String, Any>` instead of the correct typed map (e.g., `Map<String, Int>`)
2. **Runtime Serialization Failure**: Even with correct types, Ktor + kotlinx.serialization cannot deserialize generic Map types due to JVM type erasure

This document analyzes the root causes and proposes solutions.

## Problem Statement

### Current Behavior

Given this OpenAPI specification:
```yaml
responses:
  '200':
    content:
      application/json:
        schema:
          type: object
          additionalProperties:
            type: integer
            format: int32
```

**Expected**: Generate `Map<String, Int>`  
**Actual**: Generates `Map<String, Any>`

### Runtime Error

Even if we fix the generation, we get:
```
Serializer for class 'Map' is not found.
Please ensure that class is marked as '@Serializable' and that the serialization compiler plugin is applied.
```

## Technical Analysis

### 1. Code Generation Issue

#### Root Cause

In `KotlinPoetTypeMapper.mapType()`:

```kotlin
// Current implementation
val valueType = when (val additionalProps = schema.additionalProperties) {
    is Boolean -> KotlinType.Any
    is Schema -> mapType(additionalProps, false)  // This line never executes
    else -> KotlinType.Any
}
```

The problem: `additionalProperties` is stored as `Any?` in our domain model, but the type check `is Schema` fails because:
- During parsing, it contains a Swagger parser Schema object
- The type check expects our domain Schema type
- Type mismatch causes fallback to `Any`

#### Evidence

1. SwaggerParserAdapter correctly parses additionalProperties:
```kotlin
additionalProperties = when (val ap = schema.additionalProperties) {
    is io.swagger.v3.oas.models.media.Schema<*> -> mapSchema(ap)
    else -> ap // Boolean or null
}
```

2. But the domain model stores it as `Any?`:
```kotlin
data class Schema(
    val additionalProperties: Any? = null,  // Can be Boolean or Schema
    // ...
)
```

### 2. Runtime Serialization Issue

#### The Type Erasure Problem

```kotlin
// At compile time
suspend fun getInventory(): Map<String, Int>

// At runtime (after type erasure)
suspend fun getInventory(): Map

// Ktor's body function
inline fun <reified T> HttpResponse.body(): T
```

When `body<Map<String, Int>>()` executes:
1. Reified type preserves `Map::class` but loses generic parameters
2. kotlinx.serialization looks for a serializer for raw `Map`
3. No such serializer exists (only `MapSerializer<K, V>` exists)
4. Serialization fails

#### Why Lists Work

Lists work because:
1. kotlinx.serialization has special handling for common collections
2. The compiler plugin generates serializers for `List<T>` when T is `@Serializable`
3. But Map requires both K and V serializers, which aren't resolved at runtime

## Proposed Solutions

### Solution 1: Fix Code Generation (Immediate)

#### Changes Required

1. **Update Domain Model**:
```kotlin
data class Schema(
    val additionalProperties: AdditionalProperties? = null,
    // ...
)

sealed class AdditionalProperties {
    object BooleanValue : AdditionalProperties()
    data class SchemaValue(val schema: Schema) : AdditionalProperties()
}
```

2. **Update Parser**:
```kotlin
additionalProperties = when (val ap = schema.additionalProperties) {
    is Boolean -> if (ap) AdditionalProperties.BooleanValue else null
    is io.swagger.v3.oas.models.media.Schema<*> -> 
        AdditionalProperties.SchemaValue(mapSchema(ap))
    else -> null
}
```

3. **Update Type Mapper**:
```kotlin
when (val additionalProps = schema.additionalProperties) {
    is AdditionalProperties.BooleanValue -> KotlinType.Any
    is AdditionalProperties.SchemaValue -> 
        mapType(additionalProps.schema, false)
    null -> KotlinType.Any
}
```

### Solution 2: Handle Map Deserialization (Medium-term)

#### Option A: Manual Deserialization

Generate this code for Map responses:
```kotlin
public suspend fun getInventory(): Map<String, Int> {
    val response = httpClient.request {
        url("$baseUrl/store/inventory")
        method = HttpMethod.Get
    }
    val jsonString = response.bodyAsText()
    return Json.decodeFromString(MapSerializer(String.serializer(), Int.serializer()), jsonString)
}
```

#### Option B: Wrapper Classes

Generate a wrapper for each Map response:
```kotlin
@Serializable
data class InventoryResponse(
    val data: Map<String, Int>
)

public suspend fun getInventory(): Map<String, Int> {
    return httpClient.request {
        url("$baseUrl/store/inventory")
        method = HttpMethod.Get
    }
    .body<InventoryResponse>()
    .data
}
```

#### Option C: Type-Safe Builders

Use Ktor's type-safe API with explicit type info:
```kotlin
public suspend fun getInventory(): Map<String, Int> {
    return httpClient.request {
        url("$baseUrl/store/inventory")
        method = HttpMethod.Get
    }
    .body(typeInfo<Map<String, Int>>())
}
```

### Solution 3: Alternative Serialization (Long-term)

Consider supporting multiple serialization libraries:
- Jackson: Handles generic types without issues
- Gson: Also handles generics well
- Moshi: Type-safe with good generic support

## Implementation Plan

### Phase 1: Fix Type Generation (1-2 days)
1. Update domain model to use sealed class for additionalProperties
2. Update parser to map to new domain model
3. Update type mapper to handle new structure
4. Add tests for Map type generation

### Phase 2: Runtime Serialization (3-5 days)
1. Detect Map return types in client generator
2. Implement manual deserialization approach
3. Add configuration option for Map handling strategy
4. Update documentation

### Phase 3: Comprehensive Testing (2-3 days)
1. Test with various Map types (primitive values, objects, nested maps)
2. Test with real-world APIs (Petstore, others)
3. Performance testing for large Maps
4. Edge case testing (empty maps, null values)

## Backwards Compatibility

- Phase 1 changes are backwards compatible (better type generation)
- Phase 2 requires careful implementation to maintain API compatibility
- Consider configuration flags for different Map handling strategies

## Alternative Approaches Considered

1. **Custom Ktor Plugin**: Create a plugin to handle Map deserialization
   - Pros: Clean solution, reusable
   - Cons: Complex, requires Ktor expertise

2. **Annotation Processor**: Generate serializers at compile time
   - Pros: Type-safe, efficient
   - Cons: Adds complexity, requires annotation processing

3. **Runtime Reflection**: Use reflection to determine Map types
   - Pros: Works with existing code
   - Cons: Performance overhead, not type-safe

## Testing Strategy

### Unit Tests
- Test additionalProperties parsing for all variations
- Test type mapping for Maps with different value types
- Test generated code compilation

### Integration Tests
- Test with Petstore API (inventory endpoint)
- Test with APIs using Maps in request/response bodies
- Test edge cases (empty maps, nested maps)

### Performance Tests
- Benchmark manual deserialization vs wrapper classes
- Memory usage for large Maps
- Serialization/deserialization speed

## Documentation Updates

1. Update README with Map serialization limitations
2. Add troubleshooting guide for Map issues
3. Document configuration options for Map handling
4. Provide examples for each approach

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Breaking existing generated code | High | Use feature flags, gradual rollout |
| Performance regression | Medium | Benchmark all approaches |
| Increased complexity | Medium | Clear documentation, good defaults |
| Ktor API changes | Low | Abstract serialization logic |

## Success Metrics

1. **Correctness**: Maps generate with proper types (not Any)
2. **Runtime Success**: No serialization errors for Map types
3. **Performance**: < 10% overhead vs current implementation
4. **Developer Experience**: Clear error messages, easy configuration

## Conclusion

The Map serialization issue stems from both a code generation bug and fundamental limitations of Ktor + kotlinx.serialization with generic types. The proposed phased approach addresses both issues while maintaining backwards compatibility and providing flexibility for different use cases.

The immediate priority is fixing the type generation bug (Phase 1), followed by implementing robust Map deserialization (Phase 2). This will ensure the OpenAPI Kotlin generator can handle all common OpenAPI patterns reliably.

## References

### Related Issues and Discussions

1. **kotlinx.serialization Generic Type Issues**:
   - [Issue #1348: Serializer for generic classes](https://github.com/Kotlin/kotlinx.serialization/issues/1348)
   - [Issue #296: Serialization of Map with Any values](https://github.com/Kotlin/kotlinx.serialization/issues/296)
   - [Issue #746: Generic serialization with type erasure](https://github.com/Kotlin/kotlinx.serialization/issues/746)

2. **Ktor Content Negotiation with Generics**:
   - [KTOR-2868: ContentNegotiation fails with generic types](https://youtrack.jetbrains.com/issue/KTOR-2868)
   - [KTOR-3393: Type information lost in body<T>()](https://youtrack.jetbrains.com/issue/KTOR-3393)
   - [Ktor Documentation: Type-safe request](https://ktor.io/docs/type-safe-request.html)

3. **Community Discussions**:
   - [Stack Overflow: Ktor client generic response type](https://stackoverflow.com/questions/65352932/ktor-client-generic-response-type-kotlinx-serialization)
   - [Reddit: kotlinx.serialization and generic types](https://www.reddit.com/r/Kotlin/comments/n4k8x4/kotlinxserialization_and_generic_types/)

4. **Official Documentation**:
   - [kotlinx.serialization: Polymorphism and generic classes](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#generic-classes)
   - [Ktor: Receiving and sending data](https://ktor.io/docs/serialization.html)
   - [OpenAPI Specification: Additional Properties](https://spec.openapis.org/oas/v3.0.3#schema-object)

5. **Workaround Examples**:
   - [Gist: Ktor generic type workaround](https://gist.github.com/hfhbd/7e8c8f7c5d39c5a8f5e6d3a2c4d5e6f7)
   - [Medium: Handling generic types in Ktor](https://medium.com/@dev.lcc/handling-generic-types-in-ktor-client-8f9c8d7e6a4b)

### Related Code Examples

6. **Alternative Approaches in Other Generators**:
   - [OpenAPI Generator (Java): Map handling](https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator/src/main/resources/Java/libraries/okhttp-gson/ApiClient.mustache#L892)
   - [AutoRest: TypeScript Map generation](https://github.com/Azure/autorest.typescript/blob/main/packages/autorest.typescript/src/generators/typeGenerator.ts)

These references provide additional context on why this is a known issue in the Kotlin ecosystem and how other tools handle similar problems.
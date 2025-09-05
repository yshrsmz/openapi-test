# FakeApi

All URIs are relative to *http://petstore.swagger.io/v2*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**fakeInlineSchemaAnyofPath1Get**](FakeApi.md#fakeInlineSchemaAnyofPath1Get) | **GET** /fake/inline/schema/anyof/path1 |  |
| [**fakeInlineSchemaAnyofPath2Get**](FakeApi.md#fakeInlineSchemaAnyofPath2Get) | **GET** /fake/inline/schema/anyof/path2 |  |
| [**fakeInlineSchemaAnyofPath3Get**](FakeApi.md#fakeInlineSchemaAnyofPath3Get) | **GET** /fake/inline/schema/anyof/path3 |  |
| [**op1**](FakeApi.md#op1) | **POST** /fake/api/changeowner | op1 |
| [**op2**](FakeApi.md#op2) | **POST** /fake/api/changename | op2 |
| [**op3**](FakeApi.md#op3) | **POST** /fake/api/query/enum | op3 |
| [**refToRefParameter**](FakeApi.md#refToRefParameter) | **GET** /ref/ref_to_parameter |  |
| [**refToRefParameterAnyof**](FakeApi.md#refToRefParameterAnyof) | **GET** /ref/ref_to_operation_level_parameter_oneof |  |
| [**refToRefParameterOneof**](FakeApi.md#refToRefParameterOneof) | **GET** /ref/ref_to_path_level_parameter_oneof |  |
| [**responseNoRef**](FakeApi.md#responseNoRef) | **GET** /no_ref |  |
| [**responseRefToNoRef**](FakeApi.md#responseRefToNoRef) | **GET** /ref/no_ref |  |
| [**responseRefToRef**](FakeApi.md#responseRefToRef) | **GET** /ref/ref |  |


<a id="fakeInlineSchemaAnyofPath1Get"></a>
# **fakeInlineSchemaAnyofPath1Get**
> kotlin.Any fakeInlineSchemaAnyofPath1Get()



### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = FakeApi()
try {
    val result : kotlin.Any = apiInstance.fakeInlineSchemaAnyofPath1Get()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling FakeApi#fakeInlineSchemaAnyofPath1Get")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FakeApi#fakeInlineSchemaAnyofPath1Get")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="fakeInlineSchemaAnyofPath2Get"></a>
# **fakeInlineSchemaAnyofPath2Get**
> kotlin.Any fakeInlineSchemaAnyofPath2Get()



### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = FakeApi()
try {
    val result : kotlin.Any = apiInstance.fakeInlineSchemaAnyofPath2Get()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling FakeApi#fakeInlineSchemaAnyofPath2Get")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FakeApi#fakeInlineSchemaAnyofPath2Get")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="fakeInlineSchemaAnyofPath3Get"></a>
# **fakeInlineSchemaAnyofPath3Get**
> kotlin.collections.List&lt;kotlin.Any&gt; fakeInlineSchemaAnyofPath3Get()



### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = FakeApi()
try {
    val result : kotlin.collections.List<kotlin.Any> = apiInstance.fakeInlineSchemaAnyofPath3Get()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling FakeApi#fakeInlineSchemaAnyofPath3Get")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FakeApi#fakeInlineSchemaAnyofPath3Get")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.collections.List&lt;kotlin.Any&gt;**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="op1"></a>
# **op1**
> kotlin.Any op1()

op1

### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = FakeApi()
try {
    val result : kotlin.Any = apiInstance.op1()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling FakeApi#op1")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FakeApi#op1")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="op2"></a>
# **op2**
> kotlin.Any op2()

op2

### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = FakeApi()
try {
    val result : kotlin.Any = apiInstance.op2()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling FakeApi#op2")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FakeApi#op2")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="op3"></a>
# **op3**
> op3(queryEnum)

op3

### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = FakeApi()
val queryEnum : kotlin.collections.List<CodesEnum> =  // kotlin.collections.List<CodesEnum> | query enum test
try {
    apiInstance.op3(queryEnum)
} catch (e: ClientException) {
    println("4xx response calling FakeApi#op3")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FakeApi#op3")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **queryEnum** | [**kotlin.collections.List&lt;CodesEnum&gt;**](CodesEnum.md)| query enum test | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a id="refToRefParameter"></a>
# **refToRefParameter**
> kotlin.String refToRefParameter(refToUuid)



### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = FakeApi()
val refToUuid : java.util.UUID = 61864654-6e6b-4152-a62f-795fdd606bc2 // java.util.UUID | to test ref to parameter (uuid)
try {
    val result : kotlin.String = apiInstance.refToRefParameter(refToUuid)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling FakeApi#refToRefParameter")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FakeApi#refToRefParameter")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **refToUuid** | **java.util.UUID**| to test ref to parameter (uuid) | |

### Return type

**kotlin.String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a id="refToRefParameterAnyof"></a>
# **refToRefParameterAnyof**
> refToRefParameterAnyof(refToAnyof)



to test $ref to operation level parameters

### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = FakeApi()
val refToAnyof : RefToRefParameterAnyofRefToAnyofParameter =  // RefToRefParameterAnyofRefToAnyofParameter | to test ref to parameter (anyof)
try {
    apiInstance.refToRefParameterAnyof(refToAnyof)
} catch (e: ClientException) {
    println("4xx response calling FakeApi#refToRefParameterAnyof")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FakeApi#refToRefParameterAnyof")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **refToAnyof** | [**RefToRefParameterAnyofRefToAnyofParameter**](.md)| to test ref to parameter (anyof) | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a id="refToRefParameterOneof"></a>
# **refToRefParameterOneof**
> refToRefParameterOneof(refToOneof)



to test $ref to path level parameters

### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = FakeApi()
val refToOneof : RefRefToPathLevelParameterOneofRefToOneofParameter =  // RefRefToPathLevelParameterOneofRefToOneofParameter | to test ref to parameter (oneof)
try {
    apiInstance.refToRefParameterOneof(refToOneof)
} catch (e: ClientException) {
    println("4xx response calling FakeApi#refToRefParameterOneof")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FakeApi#refToRefParameterOneof")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **refToOneof** | [**RefRefToPathLevelParameterOneofRefToOneofParameter**](.md)| to test ref to parameter (oneof) | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a id="responseNoRef"></a>
# **responseNoRef**
> kotlin.String responseNoRef()



### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = FakeApi()
try {
    val result : kotlin.String = apiInstance.responseNoRef()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling FakeApi#responseNoRef")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FakeApi#responseNoRef")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**kotlin.String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a id="responseRefToNoRef"></a>
# **responseRefToNoRef**
> kotlin.String responseRefToNoRef()



### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = FakeApi()
try {
    val result : kotlin.String = apiInstance.responseRefToNoRef()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling FakeApi#responseRefToNoRef")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FakeApi#responseRefToNoRef")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**kotlin.String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a id="responseRefToRef"></a>
# **responseRefToRef**
> kotlin.String responseRefToRef()



### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = FakeApi()
try {
    val result : kotlin.String = apiInstance.responseRefToRef()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling FakeApi#responseRefToRef")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FakeApi#responseRefToRef")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**kotlin.String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain


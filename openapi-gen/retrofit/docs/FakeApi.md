# FakeApi

All URIs are relative to *http://petstore.swagger.io/v2*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**fakeInlineSchemaAnyofPath1Get**](FakeApi.md#fakeInlineSchemaAnyofPath1Get) | **GET** fake/inline/schema/anyof/path1 |  |
| [**fakeInlineSchemaAnyofPath2Get**](FakeApi.md#fakeInlineSchemaAnyofPath2Get) | **GET** fake/inline/schema/anyof/path2 |  |
| [**fakeInlineSchemaAnyofPath3Get**](FakeApi.md#fakeInlineSchemaAnyofPath3Get) | **GET** fake/inline/schema/anyof/path3 |  |
| [**op1**](FakeApi.md#op1) | **POST** fake/api/changeowner | op1 |
| [**op2**](FakeApi.md#op2) | **POST** fake/api/changename | op2 |
| [**op3**](FakeApi.md#op3) | **POST** fake/api/query/enum | op3 |
| [**refToRefParameter**](FakeApi.md#refToRefParameter) | **GET** ref/ref_to_parameter |  |
| [**refToRefParameterAnyof**](FakeApi.md#refToRefParameterAnyof) | **GET** ref/ref_to_operation_level_parameter_oneof |  |
| [**refToRefParameterOneof**](FakeApi.md#refToRefParameterOneof) | **GET** ref/ref_to_path_level_parameter_oneof |  |
| [**responseNoRef**](FakeApi.md#responseNoRef) | **GET** no_ref |  |
| [**responseRefToNoRef**](FakeApi.md#responseRefToNoRef) | **GET** ref/no_ref |  |
| [**responseRefToRef**](FakeApi.md#responseRefToRef) | **GET** ref/ref |  |





### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(FakeApi::class.java)

val result : kotlin.Any = webService.fakeInlineSchemaAnyofPath1Get()
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




### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(FakeApi::class.java)

val result : kotlin.Any = webService.fakeInlineSchemaAnyofPath2Get()
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




### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(FakeApi::class.java)

val result : kotlin.collections.List<kotlin.Any> = webService.fakeInlineSchemaAnyofPath3Get()
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


op1

### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(FakeApi::class.java)

val result : kotlin.Any = webService.op1()
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


op2

### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(FakeApi::class.java)

val result : kotlin.Any = webService.op2()
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


op3

### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(FakeApi::class.java)
val queryEnum : kotlin.collections.List<CodesEnum> =  // kotlin.collections.List<CodesEnum> | query enum test

webService.op3(queryEnum)
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




### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(FakeApi::class.java)
val refToUuid : java.util.UUID = 61864654-6e6b-4152-a62f-795fdd606bc2 // java.util.UUID | to test ref to parameter (uuid)

val result : kotlin.String = webService.refToRefParameter(refToUuid)
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




to test $ref to operation level parameters

### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(FakeApi::class.java)
val refToAnyof : RefToRefParameterAnyofRefToAnyofParameter =  // RefToRefParameterAnyofRefToAnyofParameter | to test ref to parameter (anyof)

webService.refToRefParameterAnyof(refToAnyof)
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




to test $ref to path level parameters

### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(FakeApi::class.java)
val refToOneof : RefRefToPathLevelParameterOneofRefToOneofParameter =  // RefRefToPathLevelParameterOneofRefToOneofParameter | to test ref to parameter (oneof)

webService.refToRefParameterOneof(refToOneof)
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




### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(FakeApi::class.java)

val result : kotlin.String = webService.responseNoRef()
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




### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(FakeApi::class.java)

val result : kotlin.String = webService.responseRefToNoRef()
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




### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(FakeApi::class.java)

val result : kotlin.String = webService.responseRefToRef()
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


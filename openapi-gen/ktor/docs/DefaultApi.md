# DefaultApi

All URIs are relative to *http://petstore.swagger.io/v2*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**fakeWebhooksSourcesDeletedPost**](DefaultApi.md#fakeWebhooksSourcesDeletedPost) | **POST** /fake/webhooks/sources/deleted |  |


<a id="fakeWebhooksSourcesDeletedPost"></a>
# **fakeWebhooksSourcesDeletedPost**
> fakeWebhooksSourcesDeletedPost(fakeWebhooksSourcesDeletedPostRequest)



### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.ktor.infrastructure.*
//import com.codingfeline.openapi.ktor.models.*

val apiInstance = DefaultApi()
val fakeWebhooksSourcesDeletedPostRequest : FakeWebhooksSourcesDeletedPostRequest =  // FakeWebhooksSourcesDeletedPostRequest | 
try {
    apiInstance.fakeWebhooksSourcesDeletedPost(fakeWebhooksSourcesDeletedPostRequest)
} catch (e: ClientException) {
    println("4xx response calling DefaultApi#fakeWebhooksSourcesDeletedPost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DefaultApi#fakeWebhooksSourcesDeletedPost")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **fakeWebhooksSourcesDeletedPostRequest** | [**FakeWebhooksSourcesDeletedPostRequest**](FakeWebhooksSourcesDeletedPostRequest.md)|  | [optional] |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined


# DefaultApi

All URIs are relative to *http://petstore.swagger.io/v2*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**fakeWebhooksSourcesDeletedPost**](DefaultApi.md#fakeWebhooksSourcesDeletedPost) | **POST** /fake/webhooks/sources/deleted |  |





### Example
```kotlin
// Import classes:
//import com.codingfeline.openapi.retrofit.*
//import com.codingfeline.openapi.retrofit.infrastructure.*
//import com.codingfeline.openapi.retrofit.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val fakeWebhooksSourcesDeletedPostRequest : FakeWebhooksSourcesDeletedPostRequest =  // FakeWebhooksSourcesDeletedPostRequest | 

webService.fakeWebhooksSourcesDeletedPost(fakeWebhooksSourcesDeletedPostRequest)
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


package com.codingfeline.openapi.retrofit.apis

import com.codingfeline.openapi.retrofit.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import com.codingfeline.openapi.retrofit.models.FakeWebhooksSourcesDeletedPostRequest

interface DefaultApi {
    /**
     * POST /fake/webhooks/sources/deleted
     * 
     * 
     * Responses:
     *  - 200: successful operation
     *  - 405: Invalid input
     *
     * @param fakeWebhooksSourcesDeletedPostRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("/fake/webhooks/sources/deleted")
    fun fakeWebhooksSourcesDeletedPost(@Body fakeWebhooksSourcesDeletedPostRequest: FakeWebhooksSourcesDeletedPostRequest? = null): Call<Unit>

}

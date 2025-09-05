package com.codingfeline.openapi.retrofit.apis

import com.codingfeline.openapi.retrofit.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import com.codingfeline.openapi.retrofit.models.CodesEnum
import com.codingfeline.openapi.retrofit.models.RefRefToPathLevelParameterOneofRefToOneofParameter
import com.codingfeline.openapi.retrofit.models.RefToRefParameterAnyofRefToAnyofParameter

interface FakeApi {
    /**
     * GET fake/inline/schema/anyof/path1
     * 
     * 
     * Responses:
     *  - 200: 
     *
     * @return [Call]<[kotlin.Any]>
     */
    @GET("fake/inline/schema/anyof/path1")
    fun fakeInlineSchemaAnyofPath1Get(): Call<kotlin.Any>

    /**
     * GET fake/inline/schema/anyof/path2
     * 
     * 
     * Responses:
     *  - 200: 
     *
     * @return [Call]<[kotlin.Any]>
     */
    @GET("fake/inline/schema/anyof/path2")
    fun fakeInlineSchemaAnyofPath2Get(): Call<kotlin.Any>

    /**
     * GET fake/inline/schema/anyof/path3
     * 
     * 
     * Responses:
     *  - 200: 
     *
     * @return [Call]<[kotlin.collections.List<kotlin.Any>]>
     */
    @GET("fake/inline/schema/anyof/path3")
    fun fakeInlineSchemaAnyofPath3Get(): Call<kotlin.collections.List<kotlin.Any>>

    /**
     * POST fake/api/changeowner
     * op1
     * 
     * Responses:
     *  - 201: Successful Response
     *  - 422: Validation Error
     *
     * @return [Call]<[kotlin.Any]>
     */
    @POST("fake/api/changeowner")
    fun op1(): Call<kotlin.Any>

    /**
     * POST fake/api/changename
     * op2
     * 
     * Responses:
     *  - 201: Successful Response
     *  - 422: Validation Error
     *
     * @return [Call]<[kotlin.Any]>
     */
    @POST("fake/api/changename")
    fun op2(): Call<kotlin.Any>

    /**
     * POST fake/api/query/enum
     * op3
     * 
     * Responses:
     *  - 200: Successful Response
     *
     * @param queryEnum query enum test
     * @return [Call]<[Unit]>
     */
    @POST("fake/api/query/enum")
    fun op3(@Query("query_enum") queryEnum: @JvmSuppressWildcards kotlin.collections.List<CodesEnum>): Call<Unit>

    /**
     * GET ref/ref_to_parameter
     * 
     * 
     * Responses:
     *  - 200: required to pass validation
     *
     * @param refToUuid to test ref to parameter (uuid)
     * @return [Call]<[kotlin.String]>
     */
    @GET("ref/ref_to_parameter")
    fun refToRefParameter(@Header("ref_to_uuid") refToUuid: java.util.UUID): Call<kotlin.String>

    /**
     * GET ref/ref_to_operation_level_parameter_oneof
     * 
     * to test $ref to operation level parameters
     * Responses:
     *  - 200: Successful Response
     *
     * @param refToAnyof to test ref to parameter (anyof)
     * @return [Call]<[Unit]>
     */
    @GET("ref/ref_to_operation_level_parameter_oneof")
    fun refToRefParameterAnyof(@Header("ref_to_anyof") refToAnyof: RefToRefParameterAnyofRefToAnyofParameter): Call<Unit>

    /**
     * GET ref/ref_to_path_level_parameter_oneof
     * 
     * to test $ref to path level parameters
     * Responses:
     *  - 200: Successful Response
     *
     * @param refToOneof to test ref to parameter (oneof)
     * @return [Call]<[Unit]>
     */
    @GET("ref/ref_to_path_level_parameter_oneof")
    fun refToRefParameterOneof(@Header("ref_to_oneof") refToOneof: RefRefToPathLevelParameterOneofRefToOneofParameter): Call<Unit>

    /**
     * GET no_ref
     * 
     * 
     * Responses:
     *  - 200: required to pass validation
     *
     * @return [Call]<[kotlin.String]>
     */
    @GET("no_ref")
    fun responseNoRef(): Call<kotlin.String>

    /**
     * GET ref/no_ref
     * 
     * 
     * Responses:
     *  - 200: required to pass validation
     *
     * @return [Call]<[kotlin.String]>
     */
    @GET("ref/no_ref")
    fun responseRefToNoRef(): Call<kotlin.String>

    /**
     * GET ref/ref
     * 
     * 
     * Responses:
     *  - 200: required to pass validation
     *
     * @return [Call]<[kotlin.String]>
     */
    @GET("ref/ref")
    fun responseRefToRef(): Call<kotlin.String>

}

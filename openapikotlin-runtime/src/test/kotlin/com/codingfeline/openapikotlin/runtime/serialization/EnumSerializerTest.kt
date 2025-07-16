package com.codingfeline.openapikotlin.runtime.serialization

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EnumSerializerTest {
    
    @Serializable
    enum class Status {
        @SerialName("active")
        ACTIVE,
        @SerialName("inactive")
        INACTIVE,
        @SerialName("pending")
        PENDING
    }
    
    @Serializable
    data class StatusModel(val status: Status)
    
    private val json = Json {
        ignoreUnknownKeys = true
    }
    
    @Test
    fun `should serialize enum using SerialName`() {
        val model = StatusModel(Status.ACTIVE)
        
        val serialized = json.encodeToString(model)
        
        assertThat(serialized).isEqualTo("""{"status":"active"}""")
    }
    
    @Test
    fun `should deserialize using SerialName`() {
        val jsonString = """{"status":"pending"}"""
        
        val deserialized = json.decodeFromString<StatusModel>(jsonString)
        
        assertThat(deserialized.status).isEqualTo(Status.PENDING)
    }
    
    @Test
    fun `should use SerialName annotations for flexible enum values`() {
        // OpenAPI enums are typically handled with SerialName annotations
        // which is the standard approach in kotlinx.serialization
        val jsonString1 = """{"status":"active"}"""
        val deserialized1 = json.decodeFromString<StatusModel>(jsonString1)
        assertThat(deserialized1.status).isEqualTo(Status.ACTIVE)
        
        val jsonString2 = """{"status":"inactive"}"""
        val deserialized2 = json.decodeFromString<StatusModel>(jsonString2)
        assertThat(deserialized2.status).isEqualTo(Status.INACTIVE)
    }
    
    @Test
    fun `should throw for unknown enum value`() {
        val jsonString = """{"status":"unknown"}"""
        
        assertThrows<Exception> {
            json.decodeFromString<StatusModel>(jsonString)
        }
    }
}
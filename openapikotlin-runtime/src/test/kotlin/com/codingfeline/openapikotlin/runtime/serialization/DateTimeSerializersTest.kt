package com.codingfeline.openapikotlin.runtime.serialization

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class DateTimeSerializersTest {
    
    private val json = Json {
        serializersModule = dateTimeSerializersModule
    }
    
    @Serializable
    data class DateModel(
        @Serializable(with = LocalDateSerializer::class)
        val date: LocalDate
    )
    
    @Serializable
    data class InstantModel(
        @Serializable(with = InstantSerializer::class)
        val timestamp: Instant
    )
    
    @Test
    fun `should serialize LocalDate to ISO format`() {
        val date = LocalDate(2024, 1, 15)
        val model = DateModel(date)
        
        val serialized = json.encodeToString(model)
        
        assertThat(serialized).isEqualTo("""{"date":"2024-01-15"}""")
    }
    
    @Test
    fun `should deserialize ISO format to LocalDate`() {
        val jsonString = """{"date":"2024-01-15"}"""
        
        val deserialized = json.decodeFromString<DateModel>(jsonString)
        
        assertThat(deserialized.date.year).isEqualTo(2024)
        assertThat(deserialized.date.monthNumber).isEqualTo(1)
        assertThat(deserialized.date.dayOfMonth).isEqualTo(15)
    }
    
    @Test
    fun `should serialize Instant to ISO 8601 format`() {
        val instant = Instant.parse("2024-01-15T10:30:00Z")
        val model = InstantModel(instant)
        
        val serialized = json.encodeToString(model)
        
        assertThat(serialized).isEqualTo("""{"timestamp":"2024-01-15T10:30:00Z"}""")
    }
    
    @Test
    fun `should deserialize ISO 8601 format to Instant`() {
        val jsonString = """{"timestamp":"2024-01-15T10:30:00Z"}"""
        
        val deserialized = json.decodeFromString<InstantModel>(jsonString)
        
        assertThat(deserialized.timestamp.toString()).isEqualTo("2024-01-15T10:30:00Z")
    }
    
    @Test
    fun `should handle milliseconds in Instant`() {
        val jsonString = """{"timestamp":"2024-01-15T10:30:00.123Z"}"""
        
        val deserialized = json.decodeFromString<InstantModel>(jsonString)
        
        assertThat(deserialized.timestamp.toString()).contains("2024-01-15T10:30:00")
    }
}
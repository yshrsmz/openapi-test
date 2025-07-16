package com.codingfeline.openapikotlin.runtime.serialization

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

/**
 * Serializer for LocalDate using ISO format (yyyy-MM-dd)
 */
object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }
    
    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }
}

/**
 * Serializer for Instant using ISO 8601 format
 */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
    
    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

/**
 * SerializersModule containing all date/time serializers
 */
val dateTimeSerializersModule = SerializersModule {
    contextual(LocalDate::class, LocalDateSerializer)
    contextual(Instant::class, InstantSerializer)
}
package com.codingfeline.openapikotlin.gradle.domain.value

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class KotlinTypeTest {
    
    @Test
    fun `should create simple types`() {
        val stringType = KotlinType.String
        assertThat(stringType.simpleName).isEqualTo("String")
        assertThat(stringType.packageName).isNull()
        assertThat(stringType.isNullable).isFalse()
        assertThat(stringType.isPrimitive).isTrue()
        
        val intType = KotlinType.Int
        assertThat(intType.simpleName).isEqualTo("Int")
        assertThat(intType.isPrimitive).isTrue()
    }
    
    @Test
    fun `should create nullable types`() {
        val nullableString = KotlinType.String.nullable()
        assertThat(nullableString.isNullable).isTrue()
        assertThat(nullableString.toString()).isEqualTo("String?")
        
        val nullableInt = KotlinType.Int.nullable()
        assertThat(nullableInt.toString()).isEqualTo("Int?")
    }
    
    @Test
    fun `should create collection types`() {
        val listOfString = KotlinType.List(KotlinType.String)
        assertThat(listOfString.simpleName).isEqualTo("List")
        assertThat(listOfString.typeParameters).hasSize(1)
        assertThat(listOfString.typeParameters[0]).isEqualTo(KotlinType.String)
        assertThat(listOfString.toString()).isEqualTo("List<String>")
        
        val nullableListOfInt = KotlinType.List(KotlinType.Int).nullable()
        assertThat(nullableListOfInt.toString()).isEqualTo("List<Int>?")
    }
    
    @Test
    fun `should create map types`() {
        val mapType = KotlinType.Map(KotlinType.String, KotlinType.Any)
        assertThat(mapType.simpleName).isEqualTo("Map")
        assertThat(mapType.typeParameters).hasSize(2)
        assertThat(mapType.toString()).isEqualTo("Map<String, Any>")
    }
    
    @Test
    fun `should create custom types with package`() {
        val customType = KotlinType(
            simpleName = "Pet",
            packageName = "com.example.api.models"
        )
        assertThat(customType.simpleName).isEqualTo("Pet")
        assertThat(customType.packageName).isEqualTo("com.example.api.models")
        assertThat(customType.qualifiedName).isEqualTo("com.example.api.models.Pet")
        assertThat(customType.isPrimitive).isFalse()
    }
    
    @Test
    fun `should handle date time types`() {
        val instantType = KotlinType.Instant
        assertThat(instantType.simpleName).isEqualTo("Instant")
        assertThat(instantType.packageName).isEqualTo("kotlinx.datetime")
        assertThat(instantType.qualifiedName).isEqualTo("kotlinx.datetime.Instant")
        
        val localDateType = KotlinType.LocalDate
        assertThat(localDateType.qualifiedName).isEqualTo("kotlinx.datetime.LocalDate")
    }
    
    @Test
    fun `should identify collection types`() {
        assertThat(KotlinType.List(KotlinType.String).isCollection).isTrue()
        assertThat(KotlinType.Set(KotlinType.Int).isCollection).isTrue()
        assertThat(KotlinType.String.isCollection).isFalse()
        assertThat(KotlinType.Map(KotlinType.String, KotlinType.Any).isCollection).isTrue()
    }
    
    @Test
    fun `should provide default values`() {
        assertThat(KotlinType.String.defaultValue).isEqualTo("\"\"")
        assertThat(KotlinType.Int.defaultValue).isEqualTo("0")
        assertThat(KotlinType.Boolean.defaultValue).isEqualTo("false")
        assertThat(KotlinType.List(KotlinType.String).defaultValue).isEqualTo("emptyList()")
        assertThat(KotlinType.Map(KotlinType.String, KotlinType.Any).defaultValue).isEqualTo("emptyMap()")
        assertThat(KotlinType.String.nullable().defaultValue).isEqualTo("null")
    }
}
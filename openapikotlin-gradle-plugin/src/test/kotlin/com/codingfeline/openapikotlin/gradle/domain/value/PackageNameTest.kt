package com.codingfeline.openapikotlin.gradle.domain.value

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PackageNameTest {
    
    @Test
    fun `should create valid package name`() {
        val packageName = PackageName("com.example.api")
        assertThat(packageName.value).isEqualTo("com.example.api")
        assertThat(packageName.segments).containsExactly("com", "example", "api")
    }
    
    @Test
    fun `should validate package name format`() {
        // Valid package names
        assertThat(PackageName("com.example").isValid).isTrue()
        assertThat(PackageName("com.example.api.v1").isValid).isTrue()
        assertThat(PackageName("org.openapi.generated").isValid).isTrue()
        
        // Invalid package names
        assertThrows<IllegalArgumentException> {
            PackageName("")
        }
        
        assertThrows<IllegalArgumentException> {
            PackageName("com.example.")
        }
        
        assertThrows<IllegalArgumentException> {
            PackageName(".com.example")
        }
        
        assertThrows<IllegalArgumentException> {
            PackageName("com.123example")
        }
        
        assertThrows<IllegalArgumentException> {
            PackageName("com.example-api")
        }
    }
    
    @Test
    fun `should append sub-packages`() {
        val basePackage = PackageName("com.example.api")
        
        val modelsPackage = basePackage.append("models")
        assertThat(modelsPackage.value).isEqualTo("com.example.api.models")
        
        val clientPackage = basePackage.append("client")
        assertThat(clientPackage.value).isEqualTo("com.example.api.client")
        
        val nestedPackage = basePackage.append("models", "request")
        assertThat(nestedPackage.value).isEqualTo("com.example.api.models.request")
    }
    
    @Test
    fun `should get parent package`() {
        val packageName = PackageName("com.example.api.models")
        
        val parent = packageName.parent()
        assertThat(parent?.value).isEqualTo("com.example.api")
        
        val grandParent = parent?.parent()
        assertThat(grandParent?.value).isEqualTo("com.example")
        
        val root = PackageName("com")
        assertThat(root.parent()).isNull()
    }
    
    @Test
    fun `should convert to directory path`() {
        val packageName = PackageName("com.example.api.models")
        assertThat(packageName.toPath()).isEqualTo("com/example/api/models")
    }
    
    @Test
    fun `should check if package is sub-package of another`() {
        val basePackage = PackageName("com.example.api")
        val modelsPackage = PackageName("com.example.api.models")
        val clientPackage = PackageName("com.example.api.client")
        val otherPackage = PackageName("org.other.api")
        
        assertThat(modelsPackage.isSubPackageOf(basePackage)).isTrue()
        assertThat(clientPackage.isSubPackageOf(basePackage)).isTrue()
        assertThat(otherPackage.isSubPackageOf(basePackage)).isFalse()
        assertThat(basePackage.isSubPackageOf(modelsPackage)).isFalse()
    }
}
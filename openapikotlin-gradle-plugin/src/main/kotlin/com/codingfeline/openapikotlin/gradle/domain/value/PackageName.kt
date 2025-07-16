package com.codingfeline.openapikotlin.gradle.domain.value

/**
 * Value object representing a Java/Kotlin package name
 */
data class PackageName(val value: String) {
    
    /**
     * Package segments (e.g., ["com", "example", "api"])
     */
    val segments: List<String> = value.split(".")
    
    init {
        require(value.isNotEmpty()) { "Package name cannot be empty" }
        require(!value.startsWith(".")) { "Package name cannot start with a dot" }
        require(!value.endsWith(".")) { "Package name cannot end with a dot" }
        require(!value.contains("..")) { "Package name cannot contain consecutive dots" }
        
        segments.forEach { segment ->
            require(segment.isNotEmpty()) { "Package segments cannot be empty" }
            require(isValidIdentifier(segment)) { 
                "Invalid package segment: '$segment'. Must be a valid Java identifier" 
            }
        }
    }
    
    /**
     * Checks if the package name is valid
     */
    val isValid: Boolean = true // If we get here, it's valid due to init validation
    
    /**
     * Appends sub-packages to this package
     */
    fun append(vararg subPackages: String): PackageName {
        val newValue = (segments + subPackages).joinToString(".")
        return PackageName(newValue)
    }
    
    /**
     * Gets the parent package, or null if this is a root package
     */
    fun parent(): PackageName? {
        return if (segments.size > 1) {
            PackageName(segments.dropLast(1).joinToString("."))
        } else {
            null
        }
    }
    
    /**
     * Converts the package name to a directory path
     */
    fun toPath(): String = segments.joinToString("/")
    
    /**
     * Checks if this package is a sub-package of another
     */
    fun isSubPackageOf(other: PackageName): Boolean {
        return value.startsWith(other.value) && value != other.value
    }
    
    override fun toString(): String = value
    
    companion object {
        /**
         * Checks if a string is a valid Java identifier
         */
        private fun isValidIdentifier(s: String): Boolean {
            if (s.isEmpty()) return false
            if (!s[0].isJavaIdentifierStart()) return false
            return s.drop(1).all { it.isJavaIdentifierPart() }
        }
    }
}
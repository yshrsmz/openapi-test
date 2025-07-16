package com.codingfeline.openapikotlin.gradle.infrastructure.io

import com.codingfeline.openapikotlin.gradle.application.FileWriter as IFileWriter
import com.codingfeline.openapikotlin.gradle.domain.service.GeneratedFile
import java.io.File

/**
 * File writer implementation
 */
class FileWriter : IFileWriter {
    
    override fun write(file: GeneratedFile, outputDirectory: File) {
        val targetFile = file.getFile(outputDirectory)
        
        // Create parent directories if they don't exist
        targetFile.parentFile.mkdirs()
        
        // Write the content
        targetFile.writeText(file.content)
    }
}
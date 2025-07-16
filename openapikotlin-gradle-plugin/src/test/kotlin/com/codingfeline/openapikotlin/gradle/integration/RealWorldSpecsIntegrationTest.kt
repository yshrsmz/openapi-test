package com.codingfeline.openapikotlin.gradle.integration

import com.codingfeline.openapikotlin.gradle.infrastructure.parser.SwaggerParserAdapter
import com.codingfeline.openapikotlin.gradle.infrastructure.generator.*
import com.codingfeline.openapikotlin.gradle.domain.model.HttpMethod
import com.codingfeline.openapikotlin.gradle.domain.model.OperationContext
import com.codingfeline.openapikotlin.gradle.ClientConfig
import com.codingfeline.openapikotlin.gradle.ModelsConfig
import com.codingfeline.openapikotlin.gradle.domain.service.GeneratedFile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals

class RealWorldSpecsIntegrationTest {
    
    @TempDir
    lateinit var tempDir: File
    
    private val parser = SwaggerParserAdapter()
    
    @Test
    fun `should generate code for Petstore OpenAPI spec`() {
        // Create a simplified Petstore spec
        val specContent = """
{
  "openapi": "3.0.0",
  "info": {
    "title": "Swagger Petstore",
    "version": "1.0.0"
  },
  "servers": [
    {
      "url": "https://petstore.swagger.io/v2"
    }
  ],
  "paths": {
    "/pet": {
      "post": {
        "tags": ["pet"],
        "summary": "Add a new pet to the store",
        "operationId": "addPet",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "${'$'}ref": "#/components/schemas/Pet"
              }
            }
          }
        },
        "responses": {
          "200": {
            "description": "Successful operation",
            "content": {
              "application/json": {
                "schema": {
                  "${'$'}ref": "#/components/schemas/Pet"
                }
              }
            }
          }
        }
      }
    },
    "/pet/{petId}": {
      "get": {
        "tags": ["pet"],
        "summary": "Find pet by ID",
        "operationId": "getPetById",
        "parameters": [
          {
            "name": "petId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Successful operation",
            "content": {
              "application/json": {
                "schema": {
                  "${'$'}ref": "#/components/schemas/Pet"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "Category": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "type": "string"
          }
        }
      },
      "Tag": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "type": "string"
          }
        }
      },
      "Pet": {
        "type": "object",
        "required": ["name", "photoUrls"],
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "category": {
            "${'$'}ref": "#/components/schemas/Category"
          },
          "name": {
            "type": "string",
            "example": "doggie"
          },
          "photoUrls": {
            "type": "array",
            "items": {
              "type": "string"
            }
          },
          "tags": {
            "type": "array",
            "items": {
              "${'$'}ref": "#/components/schemas/Tag"
            }
          },
          "status": {
            "type": "string",
            "description": "pet status in the store",
            "enum": ["available", "pending", "sold"]
          }
        }
      }
    }
  }
}
        """.trimIndent()
        
        val specFile = File(tempDir, "petstore.json")
        specFile.writeText(specContent)
        
        // Parse spec
        val spec = parser.parse(specFile)
        
        // Generate models
        val typeMapper = KotlinPoetTypeMapper("com.example")
        val modelGenerator = KotlinPoetModelGenerator(typeMapper, ModelsConfig())
        val modelFiles = modelGenerator.generateModels(
            spec.components?.schemas ?: emptyMap(),
            "com.example.petstore.models"
        )
        
        // Verify models were generated
        assertTrue(modelFiles.size >= 3, "Should generate at least 3 models")
        
        val petModel = modelFiles.find { it.relativePath.endsWith("Pet.kt") }
        assertTrue(petModel != null, "Should generate Pet model")
        assertTrue(petModel.content.contains("data class Pet"), "Pet should be a data class")
        assertTrue(petModel.content.contains("val name: String"), "Pet should have required name property")
        assertTrue(petModel.content.contains("val photourls: List<String>"), "Pet should have photoUrls property")
        
        // Generate client
        val clientGenerator = KotlinPoetClientGenerator(
            typeMapper = typeMapper,
            config = ClientConfig()
        )
        
        val operations = extractOperations(spec)
        val clientFile = clientGenerator.generateClient(
            spec = spec,
            operations = operations,
            packageName = "com.example.petstore.client"
        )
        
        // Verify client was generated
        assertTrue(clientFile.content.contains("class ApiClient"), "Should generate ApiClient class")
        assertTrue(clientFile.content.contains("suspend fun addPet"), "Should generate addPet method")
        assertTrue(clientFile.content.contains("suspend fun getPetById"), "Should generate getPetById method")
        
        // Write files to temp directory for inspection
        modelFiles.forEach { file ->
            val outputFile = File(tempDir, file.relativePath)
            outputFile.parentFile.mkdirs()
            outputFile.writeText(file.content)
        }
        
        val clientOutputFile = File(tempDir, clientFile.relativePath)
        clientOutputFile.parentFile.mkdirs()
        clientOutputFile.writeText(clientFile.content)
        
        println("Generated files written to: ${tempDir.absolutePath}")
    }
    
    @Test
    fun `should generate code for simplified Ory spec with special types`() {
        // Simplified Ory-like spec with special types
        val specContent = """
{
  "openapi": "3.0.0",
  "info": {
    "title": "Ory API",
    "version": "1.0.0"
  },
  "paths": {
    "/identities": {
      "get": {
        "operationId": "listIdentities",
        "responses": {
          "200": {
            "description": "Success",
            "content": {
              "application/json": {
                "schema": {
                  "type": "array",
                  "items": {
                    "${'$'}ref": "#/components/schemas/Identity"
                  }
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "Identity": {
        "type": "object",
        "properties": {
          "id": {
            "type": "string",
            "format": "uuid"
          },
          "created_at": {
            "${'$'}ref": "#/components/schemas/Time"
          },
          "updated_at": {
            "${'$'}ref": "#/components/schemas/NullTime"
          },
          "state": {
            "type": "string",
            "enum": ["active", "inactive"]
          },
          "traits": {
            "type": "object",
            "additionalProperties": true
          }
        }
      },
      "projectCors": {
        "type": "object",
        "properties": {
          "enabled": {
            "type": "boolean"
          },
          "origins": {
            "type": "array",
            "items": {
              "type": "string"
            }
          }
        }
      }
    }
  }
}
        """.trimIndent()
        
        val specFile = File(tempDir, "ory.json")
        specFile.writeText(specContent)
        
        // Parse spec
        val spec = parser.parse(specFile)
        
        // Generate models with special types support
        val typeMapper = KotlinPoetTypeMapper("com.example")
        val modelGenerator = KotlinPoetModelGenerator(typeMapper, ModelsConfig())
        val specialTypesGenerator = SpecialTypesGenerator()
        
        val modelFiles = modelGenerator.generateModels(
            spec.components?.schemas ?: emptyMap(),
            "com.example.ory.models"
        )
        
        val specialTypeFiles = specialTypesGenerator.generateModels(
            spec,
            "com.example.ory.models"
        )
        
        // Verify models were generated
        val identityModel = modelFiles.find { it.relativePath.endsWith("Identity.kt") }
        assertTrue(identityModel != null, "Should generate Identity model")
        
        val corsModel = modelFiles.find { it.relativePath.endsWith("projectCors.kt") }
        assertTrue(corsModel != null, "Should generate projectCors model")
        
        // Verify special types were handled
        if (specialTypeFiles.isNotEmpty()) {
            val specialTypesFile = specialTypeFiles[0]
            assertTrue(specialTypesFile.content.contains("typealias"), "Should contain type aliases")
        }
        
        // Generate client
        val clientGenerator = KotlinPoetClientGenerator(
            typeMapper = typeMapper,
            config = ClientConfig()
        )
        
        val operations = extractOperations(spec)
        val clientFile = clientGenerator.generateClient(
            spec = spec,
            operations = operations,
            packageName = "com.example.ory.client"
        )
        
        // Verify client was generated
        assertTrue(clientFile.content.contains("suspend fun listIdentities"), "Should generate listIdentities method")
        
        // Write all files
        (modelFiles + specialTypeFiles + listOf(clientFile)).forEach { file ->
            val outputFile = File(tempDir, file.relativePath)
            outputFile.parentFile.mkdirs()
            outputFile.writeText(file.content)
        }
    }
    
    @Test
    fun `should handle complex nested schemas and references`() {
        val specContent = """
{
  "openapi": "3.0.0",
  "info": {
    "title": "Complex API",
    "version": "1.0.0"
  },
  "paths": {
    "/users/{userId}/orders": {
      "get": {
        "operationId": "getUserOrders",
        "parameters": [
          {
            "name": "userId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          },
          {
            "name": "status",
            "in": "query",
            "schema": {
              "type": "array",
              "items": {
                "${'$'}ref": "#/components/schemas/OrderStatus"
              }
            }
          },
          {
            "name": "include",
            "in": "query",
            "schema": {
              "type": "array",
              "items": {
                "type": "string",
                "enum": ["items", "payments", "shipping"]
              }
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Success",
            "content": {
              "application/json": {
                "schema": {
                  "${'$'}ref": "#/components/schemas/OrderList"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "OrderStatus": {
        "type": "string",
        "enum": ["pending", "processing", "shipped", "delivered", "cancelled"]
      },
      "Money": {
        "type": "object",
        "required": ["amount", "currency"],
        "properties": {
          "amount": {
            "type": "number",
            "format": "decimal"
          },
          "currency": {
            "type": "string",
            "pattern": "^[A-Z]{3}${'$'}"
          }
        }
      },
      "OrderItem": {
        "type": "object",
        "properties": {
          "id": {
            "type": "string"
          },
          "product_id": {
            "type": "string"
          },
          "quantity": {
            "type": "integer",
            "minimum": 1
          },
          "unit_price": {
            "${'$'}ref": "#/components/schemas/Money"
          },
          "total_price": {
            "${'$'}ref": "#/components/schemas/Money"
          }
        }
      },
      "Order": {
        "type": "object",
        "properties": {
          "id": {
            "type": "string"
          },
          "status": {
            "${'$'}ref": "#/components/schemas/OrderStatus"
          },
          "items": {
            "type": "array",
            "items": {
              "${'$'}ref": "#/components/schemas/OrderItem"
            }
          },
          "total": {
            "${'$'}ref": "#/components/schemas/Money"
          },
          "metadata": {
            "type": "object",
            "additionalProperties": {
              "type": "string"
            }
          }
        }
      },
      "OrderList": {
        "type": "object",
        "properties": {
          "orders": {
            "type": "array",
            "items": {
              "${'$'}ref": "#/components/schemas/Order"
            }
          },
          "total_count": {
            "type": "integer"
          },
          "page": {
            "type": "integer"
          }
        }
      }
    }
  }
}
        """.trimIndent()
        
        val specFile = File(tempDir, "complex.json")
        specFile.writeText(specContent)
        
        // Parse and generate
        val spec = parser.parse(specFile)
        val typeMapper = KotlinPoetTypeMapper("com.example")
        val modelGenerator = KotlinPoetModelGenerator(typeMapper, ModelsConfig())
        
        val modelFiles = modelGenerator.generateModels(
            spec.components?.schemas ?: emptyMap(),
            "com.example.complex.models"
        )
        
        // Verify all models were generated
        println("Generated models: ${modelFiles.map { it.relativePath }}")
        assertEquals(5, modelFiles.size, "Should generate 5 models")
        
        val orderStatusEnum = modelFiles.find { it.relativePath.endsWith("OrderStatus.kt") }
        assertTrue(orderStatusEnum != null, "Should generate OrderStatus enum")
        assertTrue(orderStatusEnum.content.contains("enum class OrderStatus"), "OrderStatus should be an enum")
        
        val moneyModel = modelFiles.find { it.relativePath.endsWith("Money.kt") }
        assertTrue(moneyModel != null, "Should generate Money model")
        assertTrue(moneyModel.content.contains("val amount: Double"), "Money should have amount property")
        
        // Generate client
        val clientGenerator = KotlinPoetClientGenerator(
            typeMapper = typeMapper,
            config = ClientConfig()
        )
        
        val operations = extractOperations(spec)
        val clientFile = clientGenerator.generateClient(
            spec = spec,
            operations = operations,
            packageName = "com.example.complex.client"
        )
        
        // Verify client handles array parameters
        assertTrue(clientFile.content.contains("status: List<OrderStatus>?"), "Should handle array query parameter")
        assertTrue(clientFile.content.contains("include: List<String>?"), "Should handle enum array parameter")
    }
    
    private fun extractOperations(spec: com.codingfeline.openapikotlin.gradle.domain.model.OpenApiSpec): List<OperationContext> {
        val operations = mutableListOf<OperationContext>()
        
        spec.paths.forEach { (path, pathItem) ->
            val methodOperations = mapOf(
                HttpMethod.GET to pathItem.get,
                HttpMethod.POST to pathItem.post,
                HttpMethod.PUT to pathItem.put,
                HttpMethod.DELETE to pathItem.delete,
                HttpMethod.PATCH to pathItem.patch,
                HttpMethod.HEAD to pathItem.head,
                HttpMethod.OPTIONS to pathItem.options
            )
            
            methodOperations.forEach { (method, operation) ->
                if (operation != null) {
                    operations.add(OperationContext(
                        operation = operation,
                        path = path,
                        method = method
                    ))
                }
            }
        }
        
        return operations
    }
}
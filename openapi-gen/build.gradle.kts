import com.android.build.gradle.internal.tasks.factory.PreConfigAction
import com.android.build.gradle.internal.tasks.factory.registerTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    alias(libs.plugins.openapi)
}

tasks.register<GenerateTask>(name="buildOpenAPIRetrofitClient") {
    generatorName = "kotlin"

    inputSpec = "${rootDir}/oas/petstore.yaml"
    outputDir = "${projectDir}/retrofit"
    packageName = "com.codingfeline.openapi.retrofit"
    library = "jvm-retrofit2"
    configOptions.putAll(
        mapOf(
            "serializationLibrary" to "kotlinx_serialization",
            "omitGradlePluginVersions" to "true",
            "omitGradleWrapper" to "true",
            "generateOneOfAnyOfWrappers" to "true",
        )
    )
}

tasks.register<GenerateTask>(name="buildOpenAPIKtorClient") {
    generatorName = "kotlin"
    inputSpec = "${rootDir}/oas/petstore.yaml"
    outputDir = "${projectDir}/ktor"
    packageName = "com.codingfeline.openapi.ktor"
    library = "jvm-ktor"
    configOptions.putAll(
        mapOf(
            "dateLibrary" to "kotlinx-datetime",
            "serializationLibrary" to "kotlinx_serialization",
            "omitGradlePluginVersions" to "true",
            "omitGradleWrapper" to "true",
            "generateOneOfAnyOfWrappers" to "true",
        )
    )
}

tasks.register<GenerateTask>(name="buildOpenAPIKMPClient") {
    generatorName = "kotlin"
    inputSpec = "${rootDir}/oas/petstore.yaml"
    outputDir = "${projectDir}/multiplatform"
    packageName = "com.codingfeline.openapi.kmp"
    templateResourcePath = "${projectDir}/templates/multiplatform"
    library = "multiplatform"
    configOptions.putAll(
        mapOf(
            "dateLibrary" to "kotlinx-datetime",
            "omitGradlePluginVersions" to "true",
            "omitGradleWrapper" to "true",
//            "generateOneOfAnyOfWrappers" to "true",
        )
    )

    doFirst {
        file("${projectDir}/multiplatform").deleteRecursively()
    }
}


//task buildGoClient(type: org.openapitools.generator.gradle.plugin.tasks.GenerateTask) {
//    generatorName.set("go")
//    inputSpec.set("$rootDir/petstore-v3.0.yaml")
//    additionalProperties.set([
//        packageName: "petstore"
//    ])
//    outputDir.set("$buildDir/go")
//    configOptions.set([
//        dateLibrary: "threetenp"
//    ])
//}
//task buildKotlinClient(type: org.openapitools.generator.gradle.plugin.tasks.GenerateTask) {
//    generatorName.set("kotlin")
//    inputSpec.set("$rootDir/petstore-v3.0.yaml")
//    outputDir.set("$buildDir/kotlin")
//    apiPackage.set("org.openapitools.example.api")
//    invokerPackage.set("org.openapitools.example.invoker")
//    modelPackage.set("org.openapitools.example.model")
//    configOptions.set([
//        dateLibrary: "java8"
//    ])
//    globalProperties.set([
//        modelDocs: "false"
//    ])
//}


//openApiGenerate {
//    generatorName = "kotlin"
//    inputSpec.set("${rootDir.absolutePath}/oas/ory-client.json")
//    outputDir = "${projectDir.absolutePath}/src"
//}

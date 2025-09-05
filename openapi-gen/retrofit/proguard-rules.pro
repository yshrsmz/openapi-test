-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }

# project specific.
-keep,includedescriptorclasses class com.codingfeline.openapi.retrofit.models.**$$serializer { *; }
-keepclassmembers class com.codingfeline.openapi.retrofit.models.** { *** Companion; }
-keepclasseswithmembers class com.codingfeline.openapi.retrofit.models.** { kotlinx.serialization.KSerializer serializer(...); }

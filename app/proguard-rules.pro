# R8 rules for the release build.

# Keep line numbers so Play Console crash reports are readable
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# Gson: DTOs are filled by reflection, so their fields must keep their names.
# ---------------------------------------------------------------------------
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keep class com.example.csievent.data.remote.dto.** { *; }
-keep class com.example.csievent.data.remote.api.UpdateUserRoleRequest { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# ---------------------------------------------------------------------------
# Retrofit (2.11 bundles most rules; these cover suspend functions in full mode)
# ---------------------------------------------------------------------------
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# OkHttp
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

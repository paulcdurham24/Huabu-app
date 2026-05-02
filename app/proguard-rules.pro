# Huabu ProGuard Rules

# Keep data models for Gson/Room serialization
-keep class com.huabu.app.data.model.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Gson
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Coil
-dontwarn coil.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# NDK / JNI
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.huabu.app.core.native.HuabuNative { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Remote API response models (Gson deserialisation)
-keep class com.huabu.app.data.remote.** { *; }

# Kotlin serialization metadata
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault

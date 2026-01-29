# ProGuard rules for Bridge Monitor

# Keep BuildConfig (contains our API key reference)
-keep class com.severn.bridgemonitor.BuildConfig { *; }

# Keep data models (used for API responses)
-keep class com.severn.bridgemonitor.Models** { *; }
-keep class com.severn.bridgemonitor.Bridge { *; }
-keep class com.severn.bridgemonitor.Closure { *; }
-keep class com.severn.bridgemonitor.BridgeData { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Room (if not already added)
-keepclassmembers class androidx.room.** { *; }
-keep class androidx.room.** { *; }

# Keep Gson-related classes and methods
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }

# Keep classes that use Gson serialization
-keep class * implements com.google.gson.JsonDeserializer { *; }
-keep class * implements com.google.gson.JsonSerializer { *; }

# Keep your DataContainer model class (or any other models used)
-keep class com.example.dailyquest.models.DataContainer { *; }

-keep class com.example.dailyquest.** { *; }

# Keep annotations for Gson
-keepattributes Signature
-keepattributes *Annotation*

# Prevent obfuscation of methods with Gson annotations
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep class **.R$* { *; }

# Handle missing javax.lang.model classes
-dontwarn javax.lang.model.**

# Handle ErrorProne annotations
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.errorprone.annotations.** { *; }

# Disable optimizations temporarily for debugging
-dontshrink

-verbose
-printconfiguration proguard-config.txt

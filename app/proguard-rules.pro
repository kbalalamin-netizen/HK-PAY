# HK PAY ProGuard / R8 Keep Rules

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Keep data models used for serialization or Firebase RTDB mapping
-keep class com.example.data.model.** { *; }

# Kotlinx Serialization
-keepattributes InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.example.ui.navigation.**$$serializer { *; }
-keepclassmembers class com.example.ui.navigation.** {
    *** Companion;
}

# Firebase Realtime Database & Auth & Messaging
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <fields>;
    @com.google.firebase.database.PropertyName <methods>;
}
-keepnames class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-dontwarn okhttp3.**
-dontwarn okio.**

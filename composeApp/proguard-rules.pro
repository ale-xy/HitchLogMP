# Keep Compose runtime
-keep class androidx.compose.** { *; }
-keep class org.jetbrains.compose.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }
-keep class dev.gitlive.firebase.** { *; }

# Keep Koin
-keep class org.koin.** { *; }
-keep class kotlin.Metadata { *; }

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class org.gmautostop.hitchlogmp.**$$serializer { *; }
-keepclassmembers class org.gmautostop.hitchlogmp.** {
    *** Companion;
}
-keepclasseswithmembers class org.gmautostop.hitchlogmp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes
-keep class org.gmautostop.hitchlogmp.domain.** { *; }
-keep class org.gmautostop.hitchlogmp.data.** { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

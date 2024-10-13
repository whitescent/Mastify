# https://developer.android.com/build/shrink-code
# Preserve the line number information for debugging stack traces.
-keepattributes LineNumberTable,SourceFile

# Change access modifiers, enabling additional optimizations and additional reorganizations
# to packages in which classes are contained.
-allowaccessmodification

# Get rid of package names, makes file smaller
-repackageclasses

# Avoid Kotlinx Serialization related members in object being removed
-keepclassmembers class ** {
    public static ** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn android.os.SystemProperties

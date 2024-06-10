# https://developer.android.com/build/shrink-code
# Preserve the line number information for debugging stack traces.
-keepattributes LineNumberTable,SourceFile

# Change access modifiers, enabling additional optimizations and additional reorganizations
# to packages in which classes are contained.
-allowaccessmodification

# Get rid of package names, makes file smaller
-repackageclasses

### region: Kotlin Serialization Rules ###

-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

### endregion
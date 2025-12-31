# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static *** throwUninitializedProperty(...);
    public static *** throwUninitializedPropertyAccessException(...);
}

# Keep app activities
-keep class * extends android.app.Activity {
    <init>();
}

# Keep ADRT classes for debugging integration
-keep class com.vibe.termplugin.adrt.** {
    *;
}

# Keep Tasker plugin classes
-keep class com.vibe.termplugin.tasker.** {
    *;
}

# Keep native library methods
-keep class com.vibe.termplugin.NativeLib {
    native <methods>;
}

# Keep Shizuku integration classes
-keep class rikka.shizuku.shell.** {
    *;
}

# Keep ViewBinding classes
-keep class * implements androidx.viewbinding.ViewBinding {
    <init>();
    *** inflate(android.view.LayoutInflater);
}
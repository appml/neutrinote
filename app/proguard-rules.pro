# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\saelim\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# For issues with using Proguard with Guava <http://stackoverflow.com/questions/9120338/proguard-configuration-for-guava-with-obfuscation-and-optimization>
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn android.support.design.**
-dontwarn com.simplecityapps.**

-keep class android.support.design.widget.** { *; }
-keep class android.support.** { *; }
-keep class org.ocpsoft.prettytime.i18n.**
-keep class com.simplecityapps.recyclerview_fastscroll.** { *; }
-keep class com.appmindlab.nano.ScrollAwareFABBehavior { *; }
-keep class com.appmindlab.nano.DisplayDBEntry$MarkdownViewJavaScriptInterface

# Keep all public and protected methods that could be used by java reflection
# Source: https://www.reddit.com/r/androiddev/comments/2z0tf9/helpproblem_with_minifyenabled_when_set_true/cpeqrnz
-keepclassmembernames class * {
	public protected <methods>;
}

-keepclasseswithmembernames class * {
	native <methods>;
}

-keepclasseswithmembernames class * {
	public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
	public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
	public static **[] values();
	public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep interface android.support.** { *; }
-keep interface android.support.design.widget.** { *; }

-keepattributes JavascriptInterface

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

-keep class org.apache.http.** { *; }

-dontwarn org.apache.http.**
-dontwarn android.net.**

-printmapping mapping.txt

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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


#-printmapping mapping.txt
#
#-dontwarn com.google.android.gms.internal.*
#-dontwarn com.google.android.gms.internal.**
#-dontwarn org.acra.*
#-dontwarn org.acra.**
#
#-verbose
##-allowaccessmodification
##-optimizations !code/simplification/arithmetic,!code/allocation/variable
#
##-dontusemixedcaseclassnames
##-keepparameternames
-renamesourcefileattribute SourceFile
#-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-keepattributes LineNumberTable,SourceFile
#
-keep class com.bluecats.sdk.** {*;}
#-keep class android.** {*;}
#-keep class fr.** {*;}
#-keep class com.squareup.picasso.** {*;}
#-keep class google.** {*;}
#-keep class com.google.** {*;}
#-keep class com.androidplot.** {*;}
#-keep class org.acra.** {*;}
#-keep class com.woolworths.android.digital.food.atompoc.** {*;}
-keep class android.support.** {*;}
#-keep class butterknife.** { *; }
#-dontwarn butterknife.internal.**
#-keep class **$$ViewInjector { *; }
#
#-keepclasseswithmembernames class * {
#    @butterknife.* <fields>;
#}
#
#-keepclasseswithmembernames class * {
#    @butterknife.* <methods>;
#}
#
#-dontwarn com.squareup.okhttp.**
#-dontwarn com.squareup.picasso.**
#
#-assumenosideeffects class android.util.Log {
#    public static boolean isLoggable(java.lang.String, int);
#    public static int v(...);
#    public static int i(...);
#    public static int w(...);
#    public static int d(...);
#    public static int e(...);
#}

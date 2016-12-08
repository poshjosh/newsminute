# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Josh\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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
# READ THESE
# https://stuff.mit.edu/afs/sipb/project/android/sdk/android-sdk-linux/tools/proguard/docs/index.html#manual/introduction.html
# http://proguard.sourceforge.net/manual/usage.html

# THESE ARE THE CUSTOM RULES
#
-keepclasseswithmembers class <com.bc.android.core.ui.HtmlExtractingJavascriptInterface> {
    public *;
}

-dontwarn java.awt.**

-dontwarn sun.net.www.protocol.http.**
-dontwarn com.sun.net.ssl.internal.ssl.Provider
-dontwarn java.nio.file.**
-dontwarn javax.security.sasl.**
-dontwarn javax.security.auth.**
-dontwarn java.beans.Beans

-keep class android.support.v7.** { *; }
-keep class android.support.v4.** { *; }
-dontwarn android.support.v4.**
-dontwarn android.support.v7.**

-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

-keep class android.net.http.** { *; }
-dontwarn android.net.http.**

# Allow obfuscation of android.support.v7.internal.view.menu.**
# to avoid problem on Samsung 4.2.2 devices with appcompat v21
# see https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.internal.view.menu.*MenuBuilder*, android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

-dontwarn com.google.vending.licensing.ILicensingService
-dontwarn com.android.vending.licensing.ILicensingService

-dontwarn com.google.android.gms.internal.**
-keep class com.google.android.gms.internal.** { *; }
-keep interface com.google.android.gms.internal.** { *; }

-dontwarn com.google.android.gms.maps.internal.**
-keep class com.google.android.gms.maps.internal.** { *; }
-keep interface com.google.android.gms.maps.internal.** { *; }

-dontwarn org.apache.harmony.awt.datatransfer.DTK

-dontwarn com.bc.net.CloudFlareConnectionHandler
-keep class com.bc.net.CloudFlareConnectionHandler
-dontwarn com.bc.net.CloudFlareResponseParameters
-keep class com.bc.net.CloudFlareResponseParameters


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
-dontwarn java.awt.**
-dontwarn com.squareup.okhttp.**

-dontwarn sun.net.www.protocol.http.**
-dontwarn com.sun.net.ssl.internal.ssl.Provider
-dontwarn java.nio.file.**
-dontwarn javax.security.sasl.**
-dontwarn javax.security.auth.**
-dontwarn java.beans.Beans
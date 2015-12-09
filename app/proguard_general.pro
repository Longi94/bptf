-dontwarn android.support.**
-keep class android.support.** { *; }
-keep interface android.support.** { *; }

-dontwarn com.squareup.**
-keep class com.squareup.** { *; }
-keep interface com.squareup.** { *; }

-dontwarn com.tlongdev.bktf.util.GlideConfiguration
-keep class com.tlongdev.bktf.util.GlideConfiguration { *; }
-keep interface com.tlongdev.bktf.util.GlideConfiguration { *; }

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}
-dontobfuscate
-optimizations !code/allocation/variable

-dontwarn android.support.**
-keep class android.support.** { *; }
-keep interface android.support.** { *; }

-dontwarn com.squareup.**
-keep class com.squareup.** { *; }
-keep interface com.squareup.** { *; }

-dontwarn okio.**

#ButterKnife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

#MPAndroidCharts
-dontwarn com.github.mikephil.charting.**
-keep class com.github.mikephil.charting.** { *; }
-keep interface com.github.mikephil.charting.** { *; }

#Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

#Dart
-dontwarn com.f2prateek.dart.internal.**
-keep class **$$ExtraInjector { *; }
-keepclasseswithmembernames class * {
    @com.f2prateek.dart.* <fields>;
}

#for dart 2.0 only
-keep class **Henson { *; }
-keep class **$$IntentBuilder { *; }

#http://stackoverflow.com/questions/26566385/shared-element-activity-transition-on-android-5
-keep public class android.app.ActivityTransitionCoordinator
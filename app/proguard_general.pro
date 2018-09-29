-dontobfuscate
-optimizations !code/allocation/variable

-dontwarn okio.**

#MPAndroidCharts
-dontwarn com.github.mikephil.charting.**
-keep class com.github.mikephil.charting.** { *; }
-keep interface com.github.mikephil.charting.** { *; }

#Dart
-dontwarn com.f2prateek.dart.internal.**
-keep class **$$ExtraInjector { *; }
-keepclasseswithmembernames class * {
    @com.f2prateek.dart.* <fields>;
}

#for dart 2.0 only
-keep class **Henson { *; }
-keep class **$$IntentBuilder { *; }

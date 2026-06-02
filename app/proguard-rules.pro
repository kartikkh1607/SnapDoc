# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep source/line info for readable release stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------
# kotlinx.serialization
# Required because @Serializable types are looked up reflectively at runtime
# (companion.serializer() and synthesised $serializer fields).
# Source: https://github.com/Kotlin/kotlinx.serialization#android
# ---------------------------------------------------------------------
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep every @Serializable model in the spec catalog DTOs (defensive — the
# rules above cover the common cases, but explicit keeps are cheap insurance).
-keep,includedescriptorclasses class com.kartik.snapdoc.data.specs.model.** { *; }

# ---------------------------------------------------------------------
# ML Kit (face detection, selfie segmentation)
# ML Kit ships its own consumer rules, but several internal classes are
# loaded via reflection through Google Play Services dynamic delivery.
# ---------------------------------------------------------------------
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.mlkit.**

# ---------------------------------------------------------------------
# Hilt
# Hilt ships consumer proguard rules; this is belt-and-braces for the
# generated component classes that DI references reflectively.
# ---------------------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# ---------------------------------------------------------------------
# Google Play Billing
# ---------------------------------------------------------------------
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

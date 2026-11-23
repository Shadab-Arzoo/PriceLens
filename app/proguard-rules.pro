# PriceLens Proguard rules
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-dontwarn javax.annotation.**

# PriceLens Application & Activities
-keep class com.pricelens.** { *; }
-keep interface com.pricelens.** { *; }
-keepclasseswithmembers class * {
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <methods>;
    @javax.inject.Inject <init>(...);
}

# Hilt & Dagger
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class dagger.hilt.** { *; }
-keep class * extends androidx.activity.ComponentActivity { *; }
-keep class * extends android.app.Application { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }

# Room SQLite Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Kyant Backdrop & Liquid Glass
-keep class com.kyant.** { *; }

# Coil Image Loading & Gson Serialization
-keep class coil.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

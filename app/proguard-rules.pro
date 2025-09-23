# Kotlin
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
	public static void check*(...);
	public static void throw*(...);
}
-assumenosideeffects class java.util.Objects {
    public static ** requireNonNull(...);
}

# Strip debug log
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# Obfuscation
-repackageclasses
-allowaccessmodification

# AIDL
-keep,allowoptimization,allowobfuscation class com.drdisagree.colorblendr.service.IRootConnection { *; }
-keep,allowoptimization,allowobfuscation class com.drdisagree.colorblendr.service.IShizukuConnection { *; }
-keep,allowoptimization,allowobfuscation class com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource { *; }

# Gson
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
# === Twister Roulette R8 / ProGuard rules ===

# Domain enums are persisted by NAME via DataStore (SettingsRepositoryImpl) and
# restored with Enum.valueOf(). R8 may rename enum constants, which would break
# valueOf() at runtime. Keep these enum classes and their members intact.
-keepclassmembers enum com.lakescorp.twisterroulette.domain.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    *;
}
-keep enum com.lakescorp.twisterroulette.domain.model.** { *; }

# Hilt, Compose, DataStore, Coroutines, and Material3 ship their own consumer
# keep rules via their AARs; no additional app-level rules required for them.

# Keep generic signatures / annotations used by reflection-based libraries.
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

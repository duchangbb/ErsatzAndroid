# Default ProGuard rules (kept minimal)
# Xposed hooks 通过反射调用，不能被混淆
-keep class com.ersatz.MainHook { *; }
-keep class com.ersatz.** { *; }
-dontwarn de.robv.android.xposed.**
-keep class de.robv.android.xposed.** { *; }
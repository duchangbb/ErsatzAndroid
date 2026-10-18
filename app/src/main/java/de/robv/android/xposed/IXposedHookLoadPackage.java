package de.robv.android.xposed;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 本地 stub：仅用于编译期类型检查。
 * 运行时由 LSPosed / Xposed 框架注入真实实现，打包时不会打进 APK
 * （见 assets/xposed_init 与 AndroidManifest 的 xposedmodule 声明）。
 */
public interface IXposedHookLoadPackage {
    void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable;
}

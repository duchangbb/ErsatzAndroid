package de.robv.android.xposed.callbacks;

/** 本地 stub，仅为编译通过。 */
public class XC_LoadPackage {

    public static class LoadPackageParam {
        public String packageName;
        public String processName;
        public ClassLoader classLoader;
        public boolean isFirstApplication;
    }
}

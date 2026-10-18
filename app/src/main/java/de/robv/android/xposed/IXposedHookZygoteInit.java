package de.robv.android.xposed;

/** 本地 stub，仅为编译通过。运行时由 LSPosed 注入真实实现。 */
public interface IXposedHookZygoteInit {

    /** 与真实 Xposed API 保持字段名一致，避免反射取值失败。 */
    class StartupParam {
        public String modulePath;
        public boolean startsSystemServer;
    }

    void initZygote(StartupParam startupParam) throws Throwable;
}
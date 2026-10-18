package de.robv.android.xposed;

/**
 * 本地 stub，仅为编译通过。
 * 只实现本工程用到的方法签名，运行时由 LSPosed 框架提供真实逻辑。
 */
public final class XposedHelpers {

    private XposedHelpers() {}

    /**
     * 查找并 hook 方法。
     * 签名对齐 API 82：
     *   findAndHookMethod(String className, ClassLoader, String methodName, Object... parameterTypesAndCallback)
     * 最后一个是 XC_MethodHook，前面是参数类型。
     */
    public static XC_MethodHook.Unhook findAndHookMethod(
            String className, ClassLoader classLoader, String methodName,
            Object... parameterTypesAndCallback) {
        return null;
    }

    public static XC_MethodHook.Unhook findAndHookMethod(
            Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        return null;
    }

    public static Class<?> findClass(String className, ClassLoader classLoader) {
        return null;
    }

    public static Object getObjectField(Object obj, String fieldName) {
        return null;
    }

    public static void setObjectField(Object obj, String fieldName, Object value) {}

    public static Object callMethod(Object obj, String methodName, Object... args) {
        return null;
    }
}
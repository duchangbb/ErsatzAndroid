package de.robv.android.xposed;

import java.lang.reflect.Member;

/**
 * 本地 stub，仅为编译通过。
 * 方法与字段签名对齐 Xposed API 82，运行时由框架替换。
 */
public abstract class XC_MethodHook {

    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}

    protected void afterHookedMethod(MethodHookParam param) throws Throwable {}

    public static final class MethodHookParam {
        public Member method;
        public Object thisObject;
        public Object[] args;

        private Object result;
        private Throwable throwable;

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
            this.throwable = null;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
            this.result = null;
        }

        public boolean hasThrowable() {
            return throwable != null;
        }

        public Object getResultOrThrowable() throws Throwable {
            if (throwable != null) throw throwable;
            return result;
        }
    }

    /** 对齐 API 82 的 Unhook 内部类（本工程未直接使用，保留签名）。 */
    public static final class Unhook {
        private Unhook() {}
        public void unhook() {}
    }
}
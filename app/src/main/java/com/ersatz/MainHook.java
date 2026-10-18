package com.ersatz;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * ErsatzAndroid 核心 Hook。
 *
 * 思路对齐 iOS 的 Ersatz：
 *   - Hook TextView.setText(...)   <->  UILabel/UITextView setText
 *   - Hook PackageManager 的 label <-> SBApplication displayName
 * 在 setText 之前把文本替换掉，实现全局文本替换。
 */
public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        // 只处理有 android.widget.TextView 存在的进程（几乎全部），
        // 但跳过我们自己的模块进程，避免界面被替换。
        if ("com.ersatz".equals(lpparam.packageName)) {
            return;
        }

        try {
            hookTextView(lpparam);
        } catch (Throwable t) {
            XposedBridge.log("[Ersatz] hookTextView failed: " + t);
        }

        try {
            hookAppLabel(lpparam);
        } catch (Throwable t) {
            XposedBridge.log("[Ersatz] hookAppLabel failed: " + t);
        }
    }

    // ---------- 核心：TextView 文本替换 ----------

    private void hookTextView(final XC_LoadPackage.LoadPackageParam lp) {
        // setText(CharSequence)
        XposedHelpers.findAndHookMethod(
                "android.widget.TextView", lp.classLoader,
                "setText", CharSequence.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        CharSequence cs = (CharSequence) param.args[0];
                        CharSequence rep = replace(cs);
                        if (rep != cs) {
                            param.args[0] = rep;
                        }
                    }
                });

        // setText(CharSequence, TextView.BufferType)
        XposedHelpers.findAndHookMethod(
                "android.widget.TextView", lp.classLoader,
                "setText", CharSequence.class, android.widget.TextView.BufferType.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        CharSequence cs = (CharSequence) param.args[0];
                        CharSequence rep = replace(cs);
                        if (rep != cs) {
                            param.args[0] = rep;
                        }
                    }
                });
    }

    // ---------- App 名替换（可选）----------
    // 适配 ColorOS 15 / Android 15：改 App 名需要 hook 多个入口，
    // 因为 ColorOS 桌面不走原生 getApplicationLabel，最终多数走 loadLabel。

    private void hookAppLabel(final XC_LoadPackage.LoadPackageParam lp) {
        ClassLoader cl = lp.classLoader;

        // 入口 1：ApplicationInfo.loadLabel(PackageManager) —— 最底层，覆盖最广
        //   签名：public CharSequence loadLabel(PackageManager pm)
        try {
            XposedHelpers.findAndHookMethod(
                    "android.content.pm.ApplicationInfo", cl,
                    "loadLabel", android.content.pm.PackageManager.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Object result = param.getResult();
                            if (result instanceof CharSequence) {
                                param.setResult(replace((CharSequence) result));
                            }
                        }
                    });
        } catch (Throwable ignored) {}

        // 入口 2：ApplicationInfo.loadLabel(PackageManager) 在部分 ROM 被
        //         PackageItemInfo.loadLabel(PackageManager) 调用，
        //         这里补一份 PackageItemInfo 作为兜底。
        try {
            XposedHelpers.findAndHookMethod(
                    "android.content.pm.PackageItemInfo", cl,
                    "loadLabel", android.content.pm.PackageManager.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Object result = param.getResult();
                            if (result instanceof CharSequence) {
                                param.setResult(replace((CharSequence) result));
                            }
                        }
                    });
        } catch (Throwable ignored) {}

        // 入口 3：原生 PackageManager.getApplicationLabel（部分系统/设置页用它）
        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.ApplicationPackageManager", cl,
                    "getApplicationLabel", android.content.pm.ApplicationInfo.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Object result = param.getResult();
                            if (result instanceof CharSequence) {
                                param.setResult(replace((CharSequence) result));
                            }
                        }
                    });
        } catch (Throwable ignored) {}
    }

    // ---------- 替换逻辑 ----------

    private CharSequence replace(CharSequence cs) {
        if (cs == null || cs.length() == 0) return cs;

        List<Rule> rules = currentRules();
        if (rules == null || rules.isEmpty()) return cs;

        String origin = cs.toString();
        String replaced = Rule.applyAll(rules, origin);
        if (replaced.equals(origin)) {
            return cs; // 没变化，返回原对象
        }

        // 保留富文本 span：把新文本按纯文本生成，但旧 span 无法精确对齐时才放弃 span
        if (cs instanceof Spanned) {
            SpannableString out = new SpannableString(replaced);
            // 仅当长度未变化时尝试拷贝 span（长度变化则放弃 span，仅替换文本）
            if (cs.length() == replaced.length() && cs instanceof Spannable) {
                // 简单拷贝同位置 span（近似处理）
                try {
                    copySpans((Spannable) cs, out);
                } catch (Throwable ignored) {}
            }
            return out;
        }
        return replaced;
    }

    private static void copySpans(Spannable from, Spannable to) {
        Object[] spans = from.getSpans(0, from.length(), Object.class);
        for (Object sp : spans) {
            int start = from.getSpanStart(sp);
            int end = from.getSpanEnd(sp);
            int flags = from.getSpanFlags(sp);
            try {
                to.setSpan(sp, start, end, flags);
            } catch (Throwable ignored) {}
        }
    }

    // ---------- 规则读取（缓存 + 跨进程）----------

    private static volatile List<Rule> cachedRules;
    private static volatile long cachedTime;
    private static final long CACHE_MS = 2000; // 2 秒后重新读，尽量保证配置及时生效

    private List<Rule> currentRules() {
        long now = System.currentTimeMillis();
        if (cachedRules != null && now - cachedTime < CACHE_MS) {
            return cachedRules;
        }
        List<Rule> rules = null;
        try {
            // 直接读模块自己 data 目录下的 SharedPreferences 文件
            Context ctx = android.app.ActivityThread.currentApplication();
            if (ctx != null) {
                rules = new RuleStore(ctx).loadRules();
            }
        } catch (Throwable ignored) {}

        cachedRules = rules;
        cachedTime = now;
        return rules;
    }
}
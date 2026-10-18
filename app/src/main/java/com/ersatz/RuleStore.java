package com.ersatz;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 规则持久化。
 * 用一个 SharedPreferences 存 JSON 数组，键为 "rules"。
 * 同时提供跨进程读取能力（Hook 在其它进程运行时读取同一份配置）。
 */
public class RuleStore {
    private static final String PREFS_NAME = "ersatz_rules";
    private static final String KEY_RULES = "rules";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_PROCESS_SCOPE = "process_scope";

    private SharedPreferences prefs;

    public RuleStore(Context ctx) {
        // MODE_WORLD_READABLE 已废弃，但模块场景下系统进程可能读不到；
        // 使用默认即可，LSPosed 模块通常用自己的 data 目录。
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public synchronized List<Rule> loadRules() {
        List<Rule> list = new ArrayList<>();
        String json = prefs.getString(KEY_RULES, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Rule r = new Rule();
                r.phrase = o.optString("phrase", "");
                r.replacement = o.optString("replacement", "");
                r.caseSensitive = o.optBoolean("caseSensitive", true);
                if (!TextUtils.isEmpty(r.phrase)) {
                    list.add(r);
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

    public synchronized void saveRules(List<Rule> rules) {
        JSONArray arr = new JSONArray();
        try {
            for (Rule r : rules) {
                JSONObject o = new JSONObject();
                o.put("phrase", r.phrase == null ? "" : r.phrase);
                o.put("replacement", r.replacement == null ? "" : r.replacement);
                o.put("caseSensitive", r.caseSensitive);
                arr.put(o);
            }
        } catch (Exception ignored) {}
        prefs.edit().putString(KEY_RULES, arr.toString()).apply();
    }

    public boolean isEnabled() {
        return prefs.getBoolean(KEY_ENABLED, true);
    }

    public void setEnabled(boolean b) {
        prefs.edit().putBoolean(KEY_ENABLED, b).apply();
    }

    /** 是否替换 App 名（桌面图标名） */
    public boolean replaceAppName() {
        return prefs.getBoolean("replace_app_name", false);
    }

    public void setReplaceAppName(boolean b) {
        prefs.edit().putBoolean("replace_app_name", b).apply();
    }
}
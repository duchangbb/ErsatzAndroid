package com.ersatz;

import java.util.ArrayList;
import java.util.List;

/**
 * 单条替换规则：phrase -> replacement
 */
public class Rule {
    public String phrase;       // 要查找的文本
    public String replacement;  // 替换成什么
    public boolean caseSensitive = true;

    public Rule() {}

    public Rule(String phrase, String replacement, boolean caseSensitive) {
        this.phrase = phrase;
        this.replacement = replacement;
        this.caseSensitive = caseSensitive;
    }

    /** 单条规则替换 */
    public String apply(String input) {
        if (input == null || phrase == null || phrase.isEmpty()) return input;
        if (caseSensitive) {
            return input.replace(phrase, replacement == null ? "" : replacement);
        } else {
            return replaceIgnoreCase(input, phrase, replacement == null ? "" : replacement);
        }
    }

    private static String replaceIgnoreCase(String src, String find, String rep) {
        if (src == null || find == null || find.isEmpty()) return src;
        StringBuilder sb = new StringBuilder();
        int len = find.length();
        int i = 0;
        String lower = src.toLowerCase();
        String fl = find.toLowerCase();
        while (true) {
            int idx = lower.indexOf(fl, i);
            if (idx < 0) {
                sb.append(src.substring(i));
                break;
            }
            sb.append(src, i, idx);
            sb.append(rep);
            i = idx + len;
        }
        return sb.toString();
    }

    /** 规则列表整体替换：按 phrase 长度降序，避免短串先替换破坏长串 */
    public static String applyAll(List<Rule> rules, String input) {
        if (input == null || rules == null || rules.isEmpty()) return input;
        String cur = input;
        // 复制一份并排序
        List<Rule> sorted = new ArrayList<>(rules);
        sorted.sort((a, b) -> Integer.compare(b.phrase.length(), a.phrase.length()));
        for (Rule r : sorted) {
            cur = r.apply(cur);
        }
        return cur;
    }
}
package com.ersatz;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private RuleStore store;
    private List<Rule> rules = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new RuleStore(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        // 开关：启用
        Switch sw = new Switch(this);
        sw.setText("启用文本替换");
        sw.setChecked(store.isEnabled());
        sw.setOnCheckedChangeListener((b, v) -> store.setEnabled(v));
        root.addView(sw);

        // 开关：替换 App 名
        Switch swApp = new Switch(this);
        swApp.setText("替换应用名（桌面图标名）");
        swApp.setChecked(store.replaceAppName());
        swApp.setOnCheckedChangeListener((b, v) -> store.setReplaceAppName(v));
        root.addView(swApp);

        // 列表
        ListView list = new ListView(this);
        rules = store.loadRules();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        // 点击编辑 / 删除
        list.setOnItemClickListener((parent, view, pos, id) -> {
            showEditDialog(pos);
        });

        // 添加按钮
        android.widget.Button addBtn = new android.widget.Button(this);
        addBtn.setText("＋ 添加规则");
        addBtn.setOnClickListener(v -> showEditDialog(-1));
        root.addView(addBtn);

        setContentView(root);
    }

    private void refreshList() {
        adapter.clear();
        for (Rule r : rules) {
            String arrow = r.caseSensitive ? "=" : "≈";
            adapter.add(r.phrase + "  →  " + r.replacement);
        }
        adapter.notifyDataSetChanged();
    }

    private void refreshListAndSave() {
        store.saveRules(rules);
        refreshList();
    }

    private void showEditDialog(final int pos) {
        final Rule rule = pos >= 0 ? rules.get(pos) : new Rule("", "", true);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(48, 32, 48, 0);

        final EditText etFind = new EditText(this);
        etFind.setHint("要查找的文本 (phrase)");
        etFind.setText(rule.phrase);
        box.addView(etFind);

        final EditText etReplace = new EditText(this);
        etReplace.setHint("替换为 (replacement)");
        etReplace.setText(rule.replacement);
        box.addView(etReplace);

        final CheckBox cbCase = new CheckBox(this);
        cbCase.setText("区分大小写");
        cbCase.setChecked(rule.caseSensitive);
        box.addView(cbCase);

        new AlertDialog.Builder(this)
                .setTitle(pos >= 0 ? "编辑规则" : "添加规则")
                .setView(box)
                .setPositiveButton("保存", (d, w) -> {
                    String f = etFind.getText().toString();
                    String r = etReplace.getText().toString();
                    if (TextUtils.isEmpty(f)) {
                        Toast.makeText(this, "查找文本不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    rule.phrase = f;
                    rule.replacement = r;
                    rule.caseSensitive = cbCase.isChecked();
                    if (pos < 0) {
                        rules.add(rule);
                    }
                    refreshListAndSave();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
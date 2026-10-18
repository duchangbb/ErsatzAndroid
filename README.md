# ErsatzAndroid

把 iOS 越狱插件 Ersatz 的「全局文本替换」思路复刻到 Android 的 LSPosed 模块。

## 功能
- 全局替换任意文本（对齐 Ersatz 的 Hook UILabel/UITextView setText，这里 Hook TextView.setText）
- 支持每条规则独立区分大小写
- 可选替换应用显示名（对齐 Ersatz 的 SBApplication displayName）
- 纯 Java，无第三方依赖，设置界面直接编辑规则

## 原理（对应关系）
| iOS Ersatz | Android 本模块 |
|---|---|
| Cydia Substrate 注入 | LSPosed (Xposed API) |
| Hook UILabel setText | Hook TextView.setText(CharSequence[, BufferType]) |
| Hook SBApplication displayName | Hook PackageManager.getApplicationLabel |
| plist 规则 | SharedPreferences 存 JSON 规则数组 |

## 目录结构
```
app/src/main/
  assets/xposed_init     -> Xposed 入口声明
  java/com/ersatz/MainHook.java    核心 Hook
  java/com/ersatz/Rule.java        单条规则 + 替换引擎
  java/com/ersatz/RuleStore.java   规则持久化
  java/com/ersatz/MainActivity.java 设置界面
  AndroidManifest.xml               xposedmodule 元数据
```

## 构建（无需电脑，用 GitHub Actions 在线编译）

本工程已配置 `.github/workflows/build.yml`，只要把代码推送到 GitHub，
就会自动在云端编译出 APK，无需本地安装任何环境。

手机端操作步骤：

1. 在浏览器注册/登录 GitHub，点右上角「+」→「New repository」，
   仓库名随意（如 ErsatzAndroid），设为 Public，点「Create repository」。

2. 上传文件：进入空仓库页，点「uploading an existing file」，
   把本工程 `/sdcard/ErsatzAndroid/` 下所有文件拖进去（注意带上
   `.github/` 和 `app/` 等所有目录），点「Commit changes」。

   若文件较多，也可在手机上装 Git，按下面命令推送：
   ```
   cd ~
   cp -r /sdcard/ErsatzAndroid ~/ErsatzAndroid
   cd ~/ErsatzAndroid
   git init
   git add -A
   git commit -m "init"
   git branch -M main
   git remote add origin https://github.com/<你的用户名>/<仓库名>.git
   git push -u origin main
   ```

3. 推送成功后，GitHub 会自动开始编译。点仓库页上方「Actions」标签，
   看到「Build APK」工作流跑完（绿色对勾）即可。

4. 点进这次运行 → 页面下方「Artifacts」区域下载
   `ErsatzAndroid-debug`（一个 zip），解压得到 `app-debug.apk`。

5. 把 APK 装到已 root + LSPosed 的手机即可。

构建参数：minSdk 26，targetSdk 34，compileSdk 34，Xposed API 82，Gradle 8.2，JDK 17。

## 使用
1. 编译出 APK，装到已 root + LSPosed 的手机
2. 在 LSPosed 管理器里勾选本模块，作用域勾「系统框架」+ 想替换的目标 App
3. 打开本 App 添加规则，重启目标 App 生效

## 注意
- WebView 里的网页文本走 Chromium 渲染，Java 层 setText 拦不到，不在覆盖范围
- 替换 App 名的 hook 因 Android 版本签名差异可能需按机型微调
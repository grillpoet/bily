[English](README.md) | [简体中文](README.zh-CN.md)

# Bily

B 站第三方 Android 客户端，Kotlin + Jetpack Compose。只做搜索、播放、下载和本地收藏夹，没有广告推荐和社交，数据走 B 站公开接口。

## 功能

**搜索和浏览**
关键词搜索视频，可进 UP 主空间翻投稿，结果分页加载。搜索走 WBI 签名的 Web 接口，空间走 APP 签名的 cursor 分页。

**播放**
Media3 ExoPlayer 做内核，播放器 UI 是自己写的。支持手势快进快退、双击暂停/播放、0.5x–2x 倍速。全屏按视频实际宽高比自动切横竖屏，竖屏视频走单独的竖屏全屏分支。多P 视频支持切换，播放结束自动进下一 P。

**下载**
支持 720P 和 360P。用系统 DownloadManager 排队，下载文件存到公共 Downloads 目录，文件名带上分P 和 BV 号避免重名。

**收藏夹**
用 DataStore Preferences 存 JSON，支持多文件夹分类。一个视频只属于一个文件夹，收藏时先从原文件夹移除。导入支持合并和覆盖两种模式，按 bvid 全局去重。

**其他**
深色模式，Coil 封面缓存（RGB_565 省一半内存），分页网格滚动接近底部自动加载。

## 网络层

B 站接口有几层反爬，这里都处理了：

**WBI 签名**
从 `nav` 接口拿 img_key 和 sub_key，按固定的 64 位置换表生成 mixin_key，参数排序后拼串再 MD5，得到 w_rid。搜索和播放地址接口都用这个。

**APP 签名**
用硬编码的 appkey/appsec 对参数做 MD5，用于空间投稿这类 APP 端接口。

**Buvid 指纹**
请求 `spi` 接口拿 buvid3/buvid4 写入 cookie，配合生成的 _uuid 和 b_lsid，让请求看起来像真实浏览器。

**风控处理**
检测 code -352/-402/-412/-799，以及 data 里只有 v_voucher 的情况，抛专门的 RateLimited 异常，UI 层提示"请求太频繁"。

**节流与重试**
每个 API 分类独立节流（播放 200ms、搜索 500ms、空间 500ms），互不阻塞。RetryInterceptor 对 412/429 做指数退避重试，最多两次，带随机抖动。

**TLS 指纹**
OkHttp 配了 Chrome 的 cipher suites 和 TLS 1.3/1.2，绕开 TLS 指纹检测。

## 架构

MVVM，手动 DI。`AppContainer` 在 Application 里初始化，管理 OkHttp、Cookie、API、Repository、Downloader。ViewModel 通过 `AppViewModelFactory` 拿依赖。导航是自己写的 `Navigator`，`mutableStateListOf<Screen>` 当栈，最多 20 层，相邻重复不入栈。

    app/src/main/java/com/grillpoet/bily/
    ├── core/       常量和扩展函数
    ├── core/net/   CookieJar、拦截器、Call.await
    ├── data/       Repository、Downloader、FavoriteStore、数据模型
    ├── data/api/   API 封装、DTO、WBI/APP 签名、Buvid
    ├── di/         AppContainer
    ├── nav/        Navigator 和 Screen
    ├── ui/         AppRoot、主题、图标
    ├── ui/screen/  主页、搜索、UP 空间、视频播放
    ├── ui/vm/      ViewModel
    └── ui/widget/  播放器、卡片、对话框、无限滚动

## 构建

JDK 17，Android Studio 最近几个稳定版都行。minSdk 26，targetSdk 35，compileSdk 36。

    git clone https://github.com/grillpoet/bily.git
    cd bily
    ./gradlew assembleDebug

APK 在 `app/build/outputs/apk/debug/app-debug.apk`。Release 开了 R8 和资源压缩，需要自己配签名。

## 权限

INTERNET 用于所有网络请求。Android 13+ 需要 POST_NOTIFICATIONS，用于 DownloadManager 的下载完成通知。

## 常见问题

搜不了或者播不了，先看网络。B 站接口变动会让部分功能失效，等更新。清晰度只有 720P 和 360P，因为未登录状态下接口返回的格式有限。收藏夹数据存在本地不同步云端，建议定期导出备份。

## 许可证

MIT

[English](README.md) | [简体中文](README.zh-CN.md)

# Bily

B 站第三方 Android 客户端，基于 Kotlin 和 Jetpack Compose 构建。仅保留搜索、播放、下载和本地收藏夹管理，不包含广告、推荐流和社交模块，所有数据通过 B 站公开接口获取。

## 技术栈

UI 层用 Jetpack Compose + Material3，单向数据流配合 ViewModel 管理状态。播放内核是 Media3 ExoPlayer，支持 HLS 和 DASH 流，通过 `Player.Listener` 监听播放状态变化。网络层用 OkHttp，配合 kotlinx.serialization 做 JSON 解析，请求和响应模型定义在 `data/` 下。图片加载用 Coil，本地存储走 DataStore Preferences，收藏夹序列化为 JSON 后存储。依赖管理通过 Gradle Version Catalog，构建配置开了 R8 混淆和资源压缩。

## 架构

代码按分层组织，整体是 MVVM 结构：

    app/src/main/java/com/grillpoet/bily/
    ├── core/        常量和 Kotlin 扩展函数
    ├── data/        API 请求、下载、收藏夹、数据模型
    ├── di/          依赖注入容器
    ├── nav/         路由定义和跳转逻辑
    ├── ui/
    │   ├── screen/  主页、搜索、UP 空间、视频播放
    │   ├── theme/   主题和颜色
    │   ├── vm/      ViewModel
    │   ├── widget/  复用组件
    │   └── icon/    图标
    ├── LibiApp.kt
    └── MainActivity.kt

网络请求在 `data/` 层封装，ViewModel 负责调用并暴露 UI state，Compose 层只消费状态。DI 用轻量的手动容器，没有引入 Hilt 或 Koin。

## 功能实现

**搜索与浏览**
关键词搜索走 B 站搜索接口，结果分页加载。UP 主空间页拉取投稿列表，同样分页。

**播放**
Media3 ExoPlayer 作为播放内核。倍速范围 0.5x 到 2x，通过 `PlaybackParameters` 设置。手势快进快退基于 `PlayerView` 的触摸事件处理。全屏切换用 `Configuration` 变化监听配合 `WindowInsetsController`。音频后台播放通过 `MediaSessionService` 实现，通知栏可控。

**下载**
支持 720P 和 360P 两档清晰度。下载任务用协程在 IO 调度器上执行，通过 OkHttp 发起请求，写入应用私有目录。下载进度通过 Flow 暴露给 UI。前台服务保证后台下载不被系统回收。

**收藏夹**
数据用 DataStore Preferences 持久化，序列化为 JSON。相关逻辑集中在 `FavoriteStore.kt`。一个视频只归属一个文件夹，收藏时先从原文件夹移除再插入目标文件夹。导入时自动去重，冲突处理按视频 ID 判断。

## 构建

JDK 17，Android Studio 最近几个稳定版均可。SDK 最低 26，目标 35，编译 36。

    git clone https://github.com/grillpoet/bily.git
    cd bily
    ./gradlew assembleDebug

输出在 `app/build/outputs/apk/debug/app-debug.apk`。Release 构建启用了 R8 混淆和资源压缩，需要配置签名文件。

## 权限

INTERNET 用于网络请求，Android 10 以上存应用私有目录无需存储权限，导出到相册才需要申请媒体权限。后台下载使用 FOREGROUND_SERVICE。

## 下载和收藏夹

下载的视频存放在应用私有目录，应用内“我的下载”页面可查看。收藏夹数据用 DataStore 存在本地，JSON 格式，不同步云端。建议定期导出备份，换设备或重装时可用。

## 常见问题

搜不了或播不了先检查网络，B 站接口变动会导致部分功能失效，等更新即可。清晰度只有 720P 和 360P 是因为未登录状态下接口返回的格式有限，优先保证核心功能稳定。

## 贡献

提 Issue 或 PR 均可，报 Bug 请附上复现步骤、机型和 Android 版本。

## 许可证

MIT

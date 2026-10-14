

---

# Bily

**Bily** 是一个轻量的 Android Bilibili 第三方客户端，专注于搜索、播放和下载视频，支持自定义收藏夹管理。项目基于 Kotlin 和 Jetpack Compose 构建，采用现代化的 Android 技术栈，力求在保持体积小巧的同时提供流畅的视频消费体验。

> **Bily** — A lightweight Android client for Bilibili, focused on searching, playing, and downloading videos, with custom favorites management. Built with Kotlin and Jetpack Compose.

---

## 📖 项目简介

Bilibili 官方客户端功能繁多，对于只需要核心视频功能的用户来说较为臃肿。Bily 的目标是提供一个**轻量、专注、可定制**的替代方案，让用户可以快速搜索视频、流畅播放内容、下载喜欢的视频到本地，并通过自定义收藏夹管理自己的视频库。

本项目完全开源，不包含任何广告、推荐算法或社交功能，所有数据交互均直接与 Bilibili 公开接口通信。

---

## ✨ 功能特性

### 🔍 视频搜索与浏览
- 关键词搜索视频，快速找到感兴趣的内容
- 浏览 UP 主空间，查看创作者的全部投稿视频
- 搜索结果支持分页加载，流畅浏览大量内容

### ▶️ 视频播放
- 基于 Media3 (ExoPlayer) 的高性能播放内核
- 支持手势快进/快退，精准定位播放进度
- 播放速度调节（0.5x ~ 2.0x），适配不同观看习惯
- 一键全屏切换，自动适配横竖屏
- 音频/视频播放均支持，可在后台继续播放音频

### ⬇️ 视频下载
- 支持选择清晰度下载：**720P** / **360P**
- 下载任务后台运行，不阻塞应用其他操作
- 下载完成后自动保存到本地，可随时离线观看

### 📁 收藏夹管理
- 创建自定义收藏夹，按主题分类管理视频
- 支持收藏夹的**导入/导出**，方便迁移和备份
- 快速收藏播放中的视频，一键加入指定收藏夹

### 🎨 界面与体验
- **深色模式**支持，夜间观看更护眼
- 基于 Jetpack Compose 的声明式 UI，界面流畅响应迅速
- 内置**调试面板**，可实时查看网络请求状态和错误日志，方便排查问题

---

## 🛠️ 技术栈

| 技术 | 用途 |
|------|------|
| **Kotlin** | 主要开发语言 |
| **Jetpack Compose** | 声明式 UI 框架 |
| **Media3 (ExoPlayer)** | 视频/音频播放内核 |
| **OkHttp** | HTTP 网络请求 |
| **Kotlinx Serialization** | JSON 序列化/反序列化 |
| **Coil** | 图片异步加载 |
| **DataStore** | 偏好设置与本地数据存储 |

Media3 作为 Android 官方推荐的媒体播放库，提供了强大的格式支持和稳定的播放性能。Jetpack Compose 则通过声明式 UI 范式实现了高效的界面渲染和状态管理。

---

## 📂 项目结构

```
bily/
├── app/                          # 应用主模块
│   ├── src/main/
│   │   ├── java/                 # Kotlin 源码
│   │   │   └── ...               # 按功能分包：ui / data / player / download 等
│   │   ├── res/                  # 资源文件（布局、图片、字符串等）
│   │   └── AndroidManifest.xml   # 应用清单
│   └── build.gradle.kts          # 应用模块构建配置
├── gradle/                       # Gradle 版本目录
├── build.gradle.kts              # 根构建配置
├── gradle.properties             # Gradle 全局属性
├── settings.gradle.kts           # 项目设置
├── gradlew / gradlew.bat         # Gradle Wrapper
└── readme.md                     # 本文件
```

---

## 📋 环境要求

- **Android Studio**：Ladybug 或更高版本（推荐最新稳定版）
- **JDK**：17 或更高版本
- **Android SDK**：最低支持 API 26（Android 8.0），目标 API 35
- **Gradle**：随项目 Wrapper 提供，无需单独安装

---

## 🚀 构建与运行

### 1. 克隆仓库

```bash
git clone https://github.com/grillpoet/bily.git
cd bily
```

### 2. 使用 Android Studio 打开

打开 Android Studio → `File` → `Open` → 选择 `bily` 目录，等待 Gradle 同步完成。

### 3. 命令行构建

**Debug 版本：**

```bash
./gradlew assembleDebug
```

构建产物位于 `app/build/outputs/apk/debug/app-debug.apk`。

**Release 版本：**

```bash
./gradlew assembleRelease
```

> 注意：Release 构建需要配置签名文件。如仅用于本地测试，可直接使用 Debug 版本。

### 4. 安装到设备

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

或直接在 Android Studio 中点击 **Run** 按钮。

---

## ⬇️ 下载功能说明

Bily 的视频下载功能支持**720P** 和 **360P** 两种清晰度。下载的视频保存在应用私有目录中，可通过系统文件管理器或应用内播放器访问。

**权限说明：**

- **网络权限**：`INTERNET` — 用于搜索、播放和下载视频。
- **存储权限**：Android 10 及以上版本使用分区存储，应用私有目录无需额外权限。如需将文件导出到公共目录（如相册），需要申请 `READ_MEDIA_VIDEO` 等媒体读取权限。
- **前台服务权限**：下载任务在后台运行时可能使用前台服务，需要 `FOREGROUND_SERVICE` 权限。

---

## 📁 收藏夹管理

收藏夹数据以 JSON 格式存储在本地，支持以下操作：

- **创建收藏夹**：为不同主题创建独立文件夹
- **添加视频**：播放视频时一键收藏到指定收藏夹
- **导入收藏夹**：从 JSON 文件导入收藏数据
- **导出收藏夹**：将收藏数据导出为 JSON 文件，便于备份或迁移

---

## 🔧 调试面板

Bily 内置了调试面板，可在设置中开启。调试面板提供以下信息：

- 网络请求的完整 URL、请求方法和响应状态码
- 请求耗时统计
- 错误日志记录，包括异常堆栈信息
- 播放器状态信息（当前播放位置、缓冲状态等）

调试面板主要用于开发阶段的问题排查，普通用户无需开启。

---

## ❓ 常见问题

**Q：应用无法搜索或播放视频？**

A：请检查网络连接是否正常。Bily 依赖 Bilibili 公开接口获取数据，如果接口出现变动，可能导致部分功能暂时不可用。请关注项目更新。

**Q：下载的视频保存在哪里？**

A：下载的视频默认保存在应用私有目录中。你可以通过应用内的“我的下载”页面查看和管理已下载的视频。

**Q：为什么只有 720P 和 360P 两种清晰度？**

A：受限于 Bilibili 接口的访问权限，未登录状态下可获取的清晰度有限。Bily 优先保证核心功能的稳定性，未来可能根据实际情况增加更多清晰度选项。

**Q：收藏夹数据会丢失吗？**

A：收藏夹数据存储在本地，不会自动同步到云端。建议定期通过“导出收藏夹”功能备份数据。

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

- **报告 Bug**：请提供详细的复现步骤、设备型号和 Android 版本
- **功能建议**：请描述使用场景和期望效果
- **代码贡献**：请确保代码通过编译，并遵循项目现有的代码风格

---

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

---

## 🙏 致谢

- [Media3](https://developer.android.com/media/media3) — Android 官方媒体播放库
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — Android 声明式 UI 框架
- [OkHttp](https://square.github.io/okhttp/) — 高效的 HTTP 客户端
- [Coil](https://coil-kt.github.io/coil/) — Kotlin 图片加载库

# ============================================================================
# Bily ProGuard 规则
# 基于 dump_proguard.txt 生成
# ============================================================================

# ---- 基础属性 ----
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault

# ============================================================================
# kotlinx.serialization
# ============================================================================
-dontnote kotlinx.serialization.**

# 所有 @Serializable 类的 $$serializer
-keep,includedescriptorclasses class com.grillpoet.bily.**$$serializer { *; }
-keepclassmembers class com.grillpoet.bily.** {
    *** Companion;
}
-keepclasseswithmembers class com.grillpoet.bily.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- @Serializable 数据类 ----

# data/Model.kt
-keep class com.grillpoet.bily.data.FavoriteItem { *; }
-keep class com.grillpoet.bily.data.FavoriteFolder { *; }
-keep class com.grillpoet.bily.data.FavoriteBackup { *; }

# data/api/Dto.kt
-keep class com.grillpoet.bily.data.api.ApiResponse { *; }
-keep class com.grillpoet.bily.data.api.NavDataDto { *; }
-keep class com.grillpoet.bily.data.api.WbiImgDto { *; }
-keep class com.grillpoet.bily.data.api.VideoInfoDto { *; }
-keep class com.grillpoet.bily.data.api.OwnerDto { *; }
-keep class com.grillpoet.bily.data.api.PageDto { *; }
-keep class com.grillpoet.bily.data.api.RelatedItemDto { *; }
-keep class com.grillpoet.bily.data.api.SearchDataDto { *; }
-keep class com.grillpoet.bily.data.api.SearchItemDto { *; }
-keep class com.grillpoet.bily.data.api.SpaceDataDtoApp { *; }
-keep class com.grillpoet.bily.data.api.PlayUrlDto { *; }
-keep class com.grillpoet.bily.data.api.DurlDto { *; }

# 嵌套类：SpaceDataDtoApp.Item
# Kotlin 编译后是 SpaceDataDtoApp$Item，需要单独 keep
-keep class com.grillpoet.bily.data.api.SpaceDataDtoApp$Item { *; }

# ============================================================================
# 异常类
# ============================================================================
-keep class com.grillpoet.bily.data.api.BiliException { *; }
-keep class com.grillpoet.bily.data.api.BiliException$* { *; }

# ============================================================================
# OkHttp
# ============================================================================
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ============================================================================
# Coil
# ============================================================================
-dontwarn coil.**

# ============================================================================
# Media3
# ============================================================================
-dontwarn androidx.media3.**

# ============================================================================
# 说明
# ============================================================================
# 1. kotlinx.serialization 从 1.4.0 起自带 consumer proguard rules，
#    这里显式写一遍是为了让规则集中可见。
#
# 2. 以下类不需要 keep：
#    - com.grillpoet.bily.nav.Screen
#      只做导航状态，无反射、无序列化
#    - com.grillpoet.bily.ui.widget.StateType
#      只做 UI 状态枚举，R8 会保留 enum 值
#
# 3. AppViewModelFactory 里的 ::class.java 只是类型比较，不是反射。

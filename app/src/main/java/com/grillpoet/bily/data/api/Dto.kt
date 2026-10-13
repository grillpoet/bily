package com.grillpoet.bily.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int = -1,
    val message: String = "",
    val data: T? = null
)

@Serializable
data class NavDataDto(
    @SerialName("isLogin") val isLogin: Boolean = false,
    val uname: String = "",
    val face: String = "",
    val mid: Long = 0,
    @SerialName("wbi_img") val wbiImg: WbiImgDto = WbiImgDto()
)

@Serializable
data class WbiImgDto(
    @SerialName("img_url") val imgUrl: String = "",
    @SerialName("sub_url") val subUrl: String = ""
)

@Serializable
data class VideoInfoDto(
    val bvid: String = "",
    val aid: Long = 0,
    val cid: Long = 0,
    val title: String = "",
    val pic: String = "",
    val desc: String = "",
    val duration: Long = 0,
    val owner: OwnerDto = OwnerDto(),
    val pages: List<PageDto> = emptyList()
)

@Serializable
data class OwnerDto(val mid: Long = 0, val name: String = "", val face: String = "")

@Serializable
data class PageDto(
    val cid: Long = 0,
    val page: Int = 1,
    val part: String = "",
    val duration: Long = 0
)

@Serializable
data class RelatedItemDto(
    val bvid: String = "",
    val title: String = "",
    val pic: String = "",
    val owner: OwnerDto = OwnerDto(),
    val duration: Long = 0
)

@Serializable
data class SearchDataDto(
    val result: List<SearchItemDto> = emptyList(),
    @SerialName("numPages") val numPages: Int = 1,
    @SerialName("numResults") val numResults: Int = 0
)

@Serializable
data class SearchItemDto(
    val bvid: String = "",
    val title: String = "",
    val pic: String = "",
    val author: String = "",
    val play: Long = 0,
    val duration: String = "",
)

@Serializable
data class SpaceDataDtoApp(
    val count: Int = 0,
    @SerialName("has_next") val hasNext: Boolean = false,
    val item: List<Item> = emptyList()
) {
    @Serializable
    data class Item(
        val param: String = "",
        val title: String = "",
        val cover: String = "",
        val uri: String = "",
        val author: String = "",
        val length: String = "",
        val created: Long = 0,
        val play: Long = 0,
        val danmaku: Long = 0
    )
}

@Serializable
data class PlayUrlDto(
    val quality: Int = 0,
    @SerialName("accept_quality") val acceptQuality: List<Int> = emptyList(),
    val durl: List<DurlDto> = emptyList()
)

@Serializable
data class DurlDto(
    val url: String = "",
    @SerialName("backup_url") val backupUrl: List<String>? = null,
    val length: Long = 0
)
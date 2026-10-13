package com.grillpoet.bily.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
data class VideoCard(
    val bvid: String,
    val title: String,
    val cover: String,
    val ownerName: String = "",
    val ownerMid: Long = 0,
    val durationText: String = "",
    val aid: Long = 0
)

data class Owner(val mid: Long, val name: String, val face: String)

data class Page(
    val cid: Long,
    val index: Int,
    val part: String,
    val durationSec: Long
)

data class VideoDetail(
    val bvid: String,
    val aid: Long,
    val title: String,
    val cover: String,
    val owner: Owner,
    val pages: List<Page>
)

data class PlayStream(
    val url: String,
    val qn: Int,
    val label: String,
    val backup: Boolean
)

data class PlaySource(
    val streams: List<PlayStream>,
    val quality: Int
) {
    val primary: PlayStream? get() = streams.firstOrNull()
}

fun qualityLabel(qn: Int): String = when (qn) {
    127 -> "8K"
    126 -> "杜比视界"
    125 -> "HDR"
    120 -> "4K"
    116 -> "1080P60"
    112 -> "1080P+"
    80 -> "1080P"
    74 -> "720P60"
    64 -> "720P"
    32 -> "480P"
    16 -> "360P"
    6 -> "240P"
    else -> "${qn}P"
}

// ==================== 收藏 ====================

@Serializable
data class FavoriteItem(
    val bvid: String
)

@Serializable
data class FavoriteFolder(
    val id: String = "",
    val name: String = "",
    val createdAt: Long = 0L,
    val items: List<FavoriteItem> = emptyList()
) {
    fun contains(bvid: String): Boolean = items.any { it.bvid == bvid }
}

@Serializable
data class FavoriteBackup(
    val folders: List<FavoriteFolder> = emptyList()
)

data class ImportResult(val total: Int, val added: Int)

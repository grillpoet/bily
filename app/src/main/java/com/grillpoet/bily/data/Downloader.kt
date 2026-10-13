package com.grillpoet.bily.data

import android.app.DownloadManager
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.net.toUri
import com.grillpoet.bily.core.Constants
import com.grillpoet.bily.core.net.PersistentCookieJar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class Downloader(
    private val context: Context,
    private val cookieJar: PersistentCookieJar,
    private val httpClient: OkHttpClient
) {

    /**
     * 依次探测 streams，返回第一个可用的 URL。
     * 用 Range: bytes=0-0 探测，兼容性比 HEAD 好。
     */
    suspend fun findAvailableUrl(streams: List<PlayStream>): String? =
        withContext(Dispatchers.IO) {
            for (stream in streams) {
                try {
                    val req = Request.Builder()
                        .url(stream.url)
                        .header("Referer", Constants.REFERER)
                        .header("User-Agent", Constants.USER_AGENT)
                        .header("Range", "bytes=0-0")
                        .build()
                    httpClient.newCall(req).execute().use { resp ->
                        val ok = resp.isSuccessful || resp.code == 206
                        if (ok) return@withContext stream.url
                    }
                } catch (_: Exception) {
                    // 继续尝试
                }
            }
            null
        }

    /**
     * @param bvid        视频 BV 号
     * @param title       视频标题
     * @param partName    分P 名称
     * @param partIndex   分P 序号（从 1 开始）
     * @param totalPages  总分P 数；>1 时文件名含 P 号
     * @param url         直链
     * @param qnName      清晰度名
     */
    fun enqueue(
        bvid: String,
        title: String,
        partName: String,
        partIndex: Int,
        totalPages: Int,
        url: String,
        qnName: String
    ) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val fileName = makeFileName(title, bvid, partName, partIndex, totalPages)

        val description = if (totalPages > 1) {
            "P$partIndex · $qnName · Bily"
        } else {
            "$qnName · Bily"
        }

        val request = DownloadManager.Request(url.toUri())
            .setTitle(title)
            .setDescription(description)
            .applyVisibility()
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setMimeType("video/mp4")
            .addRequestHeader("Referer", Constants.REFERER)
            .addRequestHeader("User-Agent", Constants.USER_AGENT)
            .addRequestHeader("Cookie", cookieJar.allAsHeader("bilibili.com"))

        dm.enqueue(request)
    }

    @Suppress("DEPRECATION")
    private fun DownloadManager.Request.applyVisibility(): DownloadManager.Request =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
        } else {
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_HIDDEN
            )
        }

    private fun makeFileName(
        title: String,
        bvid: String,
        partName: String,
        partIndex: Int,
        totalPages: Int
    ): String {
        val safeTitle = sanitize(title).take(50).ifBlank { "video" }

        return if (totalPages > 1) {
            val safePart = sanitize(partName.ifBlank { "P$partIndex" }).take(30)
            "${safeTitle}_P${partIndex}_${safePart}_$bvid.mp4"
        } else {
            "${safeTitle}_$bvid.mp4"
        }
    }

    private fun sanitize(s: String): String =
        s.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .replace(Regex("\\s+"), " ")
            .trim()
}

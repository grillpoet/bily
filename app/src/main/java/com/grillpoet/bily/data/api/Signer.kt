package com.grillpoet.bily.data.api

import com.grillpoet.bily.core.Constants
import com.grillpoet.bily.core.net.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.security.MessageDigest

private fun md5(input: String): String =
    MessageDigest.getInstance("MD5").digest(input.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

// ---------------- APP 签名 ----------------

object AppSigner {
    private const val APP_KEY = "1d8b6e7d45233436"
    private const val APP_SEC = "560c52ccd288fed045859ed18bffd973"

    fun sign(params: Map<String, String>): String {
        val all = params.toMutableMap()
        all["appkey"] = APP_KEY
        val query = all.toSortedMap().entries.joinToString("&") { (k, v) ->
            "$k=${URLEncoder.encode(v, "UTF-8")}"
        }
        return "$query&sign=${md5(query + APP_SEC)}"
    }
}

// ---------------- WBI 签名 ----------------

object Wbi {

    private val TABLE = intArrayOf(
        46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35,
        27, 43, 5, 49, 33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13,
        37, 48, 7, 16, 24, 55, 40, 61, 26, 17, 0, 1, 60, 51, 30, 4,
        22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11, 36, 20, 34, 44, 52
    )

    private const val FILTER_REGEX = "[!'()*]"

    fun sign(params: Map<String, String>, imgKey: String, subKey: String): String {
        val mixinKey = mixinKey(imgKey, subKey)
        val all = params.toMutableMap()
        all["wts"] = (System.currentTimeMillis() / 1000).toString()
        val query = all.toSortedMap().entries.joinToString("&") { (k, v) ->
            "${enc(k)}=${enc(v.replace(FILTER_REGEX, ""))}"
        }
        return "$query&w_rid=${md5(query + mixinKey)}"
    }

    fun extractKey(url: String): String =
        url.substringAfterLast('/').substringBefore('.')

    private fun mixinKey(img: String, sub: String): String {
        val raw = img + sub
        val sb = StringBuilder(32)
        for (idx in TABLE) if (idx < raw.length) sb.append(raw[idx])
        return sb.toString().take(32)
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
}

class WbiKeyManager(
    private val client: OkHttpClient,
    private val json: Json,
    private val buvid: Buvid
) {
    @Volatile private var imgKey: String? = null
    @Volatile private var subKey: String? = null
    @Volatile private var timestamp: Long = 0
    private val mutex = Mutex()

    companion object {
        private const val TTL_MS = 60 * 60 * 1000L
    }

    suspend fun ensure(): Pair<String, String> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val ci = imgKey
        val cs = subKey
        if (ci != null && cs != null && now - timestamp < TTL_MS) return@withContext ci to cs

        mutex.withLock {
            val i = imgKey
            val s = subKey
            if (i != null && s != null &&
                System.currentTimeMillis() - timestamp < TTL_MS
            ) return@withLock i to s

            buvid.ensure()
            val url = "${Constants.API_BASE}/x/web-interface/nav"
            val req = Request.Builder().url(url).build()

            client.newCall(req).await().use { resp ->
                val body = resp.body?.string().orEmpty()
                val parsed = json.decodeFromString<ApiResponse<NavDataDto>>(body)
                val data = parsed.data ?: throw IllegalStateException("nav 无 data")
                val newImg = Wbi.extractKey(data.wbiImg.imgUrl)
                val newSub = Wbi.extractKey(data.wbiImg.subUrl)
                imgKey = newImg
                subKey = newSub
                timestamp = System.currentTimeMillis()
                return@withLock newImg to newSub
            }
        }
    }
}

package com.grillpoet.bily.data.api

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.grillpoet.bily.core.Constants
import com.grillpoet.bily.core.net.PersistentCookieJar
import com.grillpoet.bily.core.net.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.SecureRandom
import java.util.UUID

private val Context.deviceDataStore by preferencesDataStore(name = "device_fingerprint")

class Buvid(
    private val appContext: Context,
    private val client: OkHttpClient,
    private val json: Json,
    private val cookieJar: PersistentCookieJar
) {
    private val mutex = Mutex()
    @Volatile private var fetched = false

    companion object {
        private const val TAG = "Buvid"
        private const val HOST = "bilibili.com"
        private const val PREF_UUID = "uuid"
        private const val PREF_LSID = "b_lsid"
        private const val UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    suspend fun ensure() {
        // 短路：cookie 里已有 buvid3 直接返回，不再请求 spi
        if (cookieJar.has(HOST, "buvid3")) {
            fetched = true
            return
        }
        if (fetched) return

        mutex.withLock {
            // 双重检查
            if (cookieJar.has(HOST, "buvid3")) {
                fetched = true
                return@withLock
            }
            if (fetched) return@withLock

            try {
                val url = "${Constants.API_BASE}/x/frontend/finger/spi"
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", UA)
                    .header("Referer", "https://www.bilibili.com/")
                    .header("Origin", "https://www.bilibili.com")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .build()

                withContext(Dispatchers.IO) {
                    client.newCall(req).await().use { resp ->
                        val body = resp.body?.string().orEmpty()
                        val root = json.parseToJsonElement(body).jsonObject
                        val data = root["data"]?.jsonObject ?: run {
                            Log.w(TAG, "spi returned no data: $body")
                            return@use
                        }

                        val b3 = data["b_3"]?.jsonPrimitive?.content.orEmpty()
                        val b4 = data["b_4"]?.jsonPrimitive?.content

                        if (b3.isEmpty()) {
                            Log.w(TAG, "spi returned empty b_3")
                            return@use
                        }

                        val buvid3 = if (b3.endsWith("infoc")) b3 else "${b3}infoc"

                        val firstTime = !cookieJar.has(HOST, "buvid3")
                        cookieJar.put(HOST, "buvid3", buvid3)

                        if (firstTime) {
                            cookieJar.put(
                                HOST, "b_nut",
                                (System.currentTimeMillis() / 1000).toString()
                            )
                        }

                        if (!b4.isNullOrEmpty()) {
                            cookieJar.put(HOST, "buvid4", b4)
                        }

                        cookieJar.put(HOST, "_uuid", getOrCreateUuid())
                        cookieJar.put(HOST, "b_lsid", getOrCreateLsid())
                        cookieJar.put(HOST, "CURRENT_FNVAL", "4048")

                        fetched = true
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "ensure failed", e)
            }
        }
    }

    private suspend fun getOrCreateUuid(): String {
        val key = stringPreferencesKey(PREF_UUID)
        val existing = appContext.deviceDataStore.data.first()[key]
        if (!existing.isNullOrEmpty()) return existing
        val uuid = UUID.randomUUID().toString().uppercase()
        appContext.deviceDataStore.edit { it[key] = uuid }
        return uuid
    }

    private suspend fun getOrCreateLsid(): String {
        val key = stringPreferencesKey(PREF_LSID)
        val existing = appContext.deviceDataStore.data.first()[key]
        if (!existing.isNullOrEmpty()) return existing
        val random = SecureRandom()
        val hex8 = String.format("%08X", random.nextInt())
        val tsHex = (System.currentTimeMillis() / 1000).toString(16).uppercase()
        val lsid = "${hex8}_$tsHex"
        appContext.deviceDataStore.edit { it[key] = lsid }
        return lsid
    }
}

package com.grillpoet.bily.di

import android.app.Application
import com.grillpoet.bily.core.net.HeaderInterceptor
import com.grillpoet.bily.core.net.PersistentCookieJar
import com.grillpoet.bily.core.net.RetryInterceptor
import com.grillpoet.bily.data.BiliRepository
import com.grillpoet.bily.data.Downloader
import com.grillpoet.bily.data.FavoriteStore
import com.grillpoet.bily.data.api.BiliApi
import com.grillpoet.bily.data.api.Buvid
import com.grillpoet.bily.data.api.WbiKeyManager
import kotlinx.serialization.json.Json
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.util.concurrent.TimeUnit

class AppContainer(app: Application) {

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    val cookieJar = PersistentCookieJar(app)

    val okHttp: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(HeaderInterceptor())
        .addInterceptor(RetryInterceptor(maxRetries = 2, baseDelayMs = 500L))
        .connectionSpecs(listOf(chromeConnectionSpec()))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val buvid = Buvid(app, okHttp, json, cookieJar)
    private val wbiKey = WbiKeyManager(okHttp, json, buvid)
    private val api = BiliApi(okHttp, json, wbiKey, buvid)

    val biliRepo = BiliRepository(api)

    private val favoriteStore = FavoriteStore(app, json)
    val favoriteRepo: FavoriteStore = favoriteStore

    val downloader = Downloader(app, cookieJar, okHttp)

    private fun chromeConnectionSpec(): ConnectionSpec =
        ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .cipherSuites(
                CipherSuite.TLS_AES_128_GCM_SHA256,
                CipherSuite.TLS_AES_256_GCM_SHA384,
                CipherSuite.TLS_CHACHA20_POLY1305_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256
            )
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
            .build()
}

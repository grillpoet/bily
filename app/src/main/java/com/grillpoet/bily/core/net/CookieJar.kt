package com.grillpoet.bily.core.net

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class PersistentCookieJar(context: Context) : CookieJar {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bili_cookies", Context.MODE_PRIVATE)
    private val lock = Any()
    private val cache = mutableMapOf<String, MutableList<Cookie>>()

    init {
        prefs.all.forEach { (key, value) ->
            if (value is String && value.isNotBlank()) {
                val url = "https://$key/".toHttpUrlOrNull() ?: return@forEach
                val cookies = value.split("\n").mapNotNull { Cookie.parse(url, it) }
                if (cookies.isNotEmpty()) cache[key] = cookies.toMutableList()
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        synchronized(lock) {
            for (c in cookies) {
                val domain = c.domain.ifEmpty { url.host }
                val list = cache.getOrPut(domain) { mutableListOf() }
                list.removeAll { it.name == c.name && it.path == c.path }
                list.add(c)
                persist(domain)
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        synchronized(lock) {
            val host = url.host
            val now = System.currentTimeMillis()
            return cache.values.flatten().filter { c ->
                val d = c.domain.removePrefix(".")
                val domainMatch = host == d || host.endsWith(".$d")
                domainMatch && (c.expiresAt == Long.MAX_VALUE || c.expiresAt > now)
            }
        }
    }

    fun put(domain: String, name: String, value: String, path: String = "/") {
        synchronized(lock) {
            val cookie = Cookie.Builder()
                .domain(domain).path(path).name(name).value(value).build()
            val list = cache.getOrPut(domain) { mutableListOf() }
            list.removeAll { it.name == name && it.path == path }
            list.add(cookie)
            persist(domain)
        }
    }

    fun has(domain: String, name: String): Boolean {
        synchronized(lock) {
            return cache.values.flatten().any {
                it.name == name &&
                        it.domain.removePrefix(".").endsWith(domain.removePrefix("."))
            }
        }
    }

    fun allAsHeader(domain: String): String {
        synchronized(lock) {
            val target = domain.removePrefix(".")
            return cache.values.flatten()
                .filter { it.domain.removePrefix(".").endsWith(target) }
                .joinToString("; ") { "${it.name}=${it.value}" }
        }
    }

    private fun persist(domain: String) {
        val list = cache[domain] ?: return
        prefs.edit { putString(domain, list.joinToString("\n") { it.toString() }) }
    }
}
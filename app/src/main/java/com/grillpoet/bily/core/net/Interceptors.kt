package com.grillpoet.bily.core.net

import okhttp3.Interceptor
import okhttp3.Response
import kotlin.math.pow
import kotlin.random.Random

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Accept", "application/json, text/plain, */*")
            .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
            .header("Sec-Fetch-Dest", "empty")
            .header("Sec-Fetch-Mode", "cors")
            .header("Sec-Fetch-Site", "same-site")

        if (original.header("Referer") == null) {
            builder.header("Referer", "https://www.bilibili.com/")
        }
        if (original.header("Origin") == null) {
            builder.header("Origin", "https://www.bilibili.com")
        }

        return chain.proceed(builder.build())
    }
}

class RetryInterceptor(
    private val maxRetries: Int = 2,
    private val baseDelayMs: Long = 500L
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastException: Exception? = null
        for (attempt in 0..maxRetries) {
            try {
                val response = chain.proceed(request)
                if (response.code == 412 || response.code == 429) {
                    response.close()
                    if (attempt < maxRetries) {
                        Thread.sleep(backoff(attempt))
                        continue
                    }
                }
                return response
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries) Thread.sleep(backoff(attempt))
            }
        }
        throw lastException ?: IllegalStateException("request failed")
    }

    private fun backoff(attempt: Int): Long {
        val base = baseDelayMs * 2.0.pow(attempt).toLong()
        return base + Random.nextLong(0, baseDelayMs)
    }
}
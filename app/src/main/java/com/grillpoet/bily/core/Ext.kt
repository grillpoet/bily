package com.grillpoet.bily.core

object DurationUtils {
    fun format(seconds: Long): String {
        if (seconds <= 0) return "0:00"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%d:%02d".format(m, s)
    }

    /**
     * 处理 B 站返回的各种字符串时长：
     *   "1:5"    -> "1:05"
     *   "1:5:3"  -> "1:05:03"
     *   "125"    -> "2:05"（纯数字当秒数）
     *   "12:34"  -> "12:34"
     *   其他      -> 原样返回
     */
    fun normalize(raw: String): String {
        if (raw.isBlank()) return ""

        if (Regex("^\\d+(:\\d+){1,2}$").matches(raw)) {
            val parts = raw.split(":")
            val first = parts[0].toLongOrNull()?.toString() ?: parts[0]
            return when (parts.size) {
                2 -> "$first:${parts[1].padStart(2, '0')}"
                3 -> "$first:${parts[1].padStart(2, '0')}:${parts[2].padStart(2, '0')}"
                else -> raw
            }
        }

        val n = raw.toLongOrNull()
        if (n != null && n > 0) return format(n)
        return raw
    }
}

fun normalizeCover(url: String): String = when {
    url.isBlank() -> ""
    url.startsWith("//") -> "https:$url"
    url.startsWith("http://") -> "https://${url.removePrefix("http://")}"
    else -> url
}

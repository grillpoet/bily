package com.grillpoet.bily.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

private val Context.favoriteDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "favorite_store")

class FavoriteStore(
    context: Context,
    private val json: Json
) {
    private val dataStore = context.favoriteDataStore
    private val key = stringPreferencesKey("folders")

    val flow: Flow<List<FavoriteFolder>> = dataStore.data.map { decode(it[key]) }

    suspend fun listFolders(): List<FavoriteFolder> = flow.first()

    suspend fun createFolder(name: String): FavoriteFolder? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null
        var created: FavoriteFolder? = null
        dataStore.edit { prefs ->
            val current = decode(prefs[key])
            if (current.any { it.name == trimmed }) return@edit
            val folder = FavoriteFolder(
                id = UUID.randomUUID().toString(),
                name = trimmed,
                createdAt = System.currentTimeMillis(),
                items = emptyList()
            )
            prefs[key] = encode(current + folder)
            created = folder
        }
        return created
    }

    suspend fun deleteFolders(ids: Set<String>) {
        if (ids.isEmpty()) return
        dataStore.edit { prefs ->
            prefs[key] = encode(decode(prefs[key]).filterNot { it.id in ids })
        }
    }

    /**
     * 收藏到指定文件夹。
     * 一个视频只属于一个文件夹：先从所有夹移除，再加入目标夹。
     */
    suspend fun addToFolder(folderId: String, bvid: String): Boolean {
        if (bvid.isEmpty()) return false
        var ok = false
        dataStore.edit { prefs ->
            val current = decode(prefs[key])
            if (current.none { it.id == folderId }) return@edit

            val updated = current.map { f ->
                if (f.id == folderId) {
                    f.copy(
                        items = f.items.filterNot { it.bvid == bvid } + FavoriteItem(bvid)
                    )
                } else {
                    f.copy(items = f.items.filterNot { it.bvid == bvid })
                }
            }
            prefs[key] = encode(updated)
            ok = true
        }
        return ok
    }

    suspend fun removeFromAll(bvid: String) {
        if (bvid.isEmpty()) return
        dataStore.edit { prefs ->
            prefs[key] = encode(
                decode(prefs[key]).map { f ->
                    f.copy(items = f.items.filterNot { it.bvid == bvid })
                }
            )
        }
    }

    suspend fun removeFromFolder(folderId: String, bvids: Set<String>) {
        if (bvids.isEmpty()) return
        dataStore.edit { prefs ->
            prefs[key] = encode(
                decode(prefs[key]).map { f ->
                    if (f.id == folderId) {
                        f.copy(items = f.items.filterNot { it.bvid in bvids })
                    } else f
                }
            )
        }
    }

    suspend fun findFolderOf(bvid: String): FavoriteFolder? =
        listFolders().firstOrNull { it.contains(bvid) }

    suspend fun isFavorited(bvid: String): Boolean =
        listFolders().any { it.contains(bvid) }

    suspend fun exportJson(): String = encode(listFolders())

    suspend fun importJson(text: String, replace: Boolean): ImportResult {
        val incoming = parseBackup(text)

        // 全局 bvid 去重：一个视频只保留在一个文件夹
        val seen = mutableSetOf<String>()
        val normalized = incoming.map { folder ->
            val kept = folder.items
                .filter { it.bvid.isNotEmpty() && seen.add(it.bvid) }
            folder.copy(
                id = folder.id.ifEmpty { UUID.randomUUID().toString() },
                items = kept
            )
        }
        val totalIncoming = normalized.sumOf { it.items.size }

        if (replace) {
            dataStore.edit { prefs -> prefs[key] = encode(normalized) }
            return ImportResult(totalIncoming, totalIncoming)
        }

        var added = 0
        dataStore.edit { prefs ->
            val existing = decode(prefs[key]).toMutableList()
            val existingBvids = existing
                .flatMap { it.items }
                .mapTo(HashSet()) { it.bvid }

            for (f in normalized) {
                val idx = existing.indexOfFirst { it.name == f.name }
                if (idx >= 0) {
                    val old = existing[idx]
                    val fresh = f.items.filter { it.bvid !in existingBvids }
                    added += fresh.size
                    fresh.forEach { existingBvids.add(it.bvid) }
                    existing[idx] = old.copy(items = old.items + fresh)
                } else {
                    added += f.items.size
                    f.items.forEach { existingBvids.add(it.bvid) }
                    existing.add(f)
                }
            }
            prefs[key] = encode(existing)
        }
        return ImportResult(totalIncoming, added)
    }

    // ---------- 内部 ----------

    private fun encode(folders: List<FavoriteFolder>): String =
        json.encodeToString(FavoriteBackup(folders = folders))

    private fun decode(raw: String?): List<FavoriteFolder> {
        if (raw.isNullOrEmpty()) return emptyList()
        return try {
            json.decodeFromString<FavoriteBackup>(raw).folders
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseBackup(text: String): List<FavoriteFolder> {
        runCatching {
            return json.decodeFromString<FavoriteBackup>(text).folders
        }
        runCatching {
            return json.decodeFromString<List<FavoriteFolder>>(text)
        }
        throw IllegalArgumentException("文件格式不正确")
    }
}

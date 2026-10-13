package com.grillpoet.bily.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grillpoet.bily.data.BiliRepository
import com.grillpoet.bily.data.VideoCard
import com.grillpoet.bily.data.api.BiliException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val keyword: String = "",
    val items: List<VideoCard> = emptyList(),
    val page: Int = 0,
    val hasMore: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val searched: Boolean = false
)

class SearchViewModel(
    private val repo: BiliRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var job: Job? = null

    fun search(keyword: String) {
        val kw = keyword.trim()
        if (kw.isEmpty()) return
        if (kw == _state.value.keyword && _state.value.items.isNotEmpty()) return

        job?.cancel()
        _state.value = SearchUiState(keyword = kw, loading = true, searched = true)
        job = viewModelScope.launch {
            delay(250)
            loadPage(1, replace = true)
        }
    }

    fun loadMore() {
        val s = _state.value
        if (s.loading || !s.hasMore || s.keyword.isEmpty()) return
        job = viewModelScope.launch { loadPage(s.page + 1, replace = false) }
    }

    private suspend fun loadPage(page: Int, replace: Boolean) {
        _state.update { it.copy(loading = true, error = null) }
        repo.search(_state.value.keyword, page)
            .onSuccess { result ->
                _state.update { s ->
                    val newItems = if (replace) result.items
                    else (s.items + result.items).distinctBy { it.bvid }
                    val noGrowth = !replace && newItems.size == s.items.size
                    s.copy(
                        items = newItems,
                        page = page,
                        hasMore = !noGrowth && page < result.totalPages && result.items.isNotEmpty(),
                        loading = false
                    )
                }
            }
            .onFailure { e ->
                val rateLimited = e is BiliException.RateLimited
                _state.update {
                    it.copy(
                        loading = false,
                        error = if (rateLimited) "请求太频繁，请稍后再试"
                        else (e.message ?: "搜索失败")
                    )
                }
            }
    }
}

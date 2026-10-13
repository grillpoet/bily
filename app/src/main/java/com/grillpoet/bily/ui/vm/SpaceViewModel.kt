package com.grillpoet.bily.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grillpoet.bily.data.BiliRepository
import com.grillpoet.bily.data.VideoCard
import com.grillpoet.bily.data.api.BiliException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SpaceUiState(
    val mid: Long = 0,
    val name: String = "",
    val items: List<VideoCard> = emptyList(),
    val page: Int = 0,
    val hasMore: Boolean = true,
    val loading: Boolean = false,
    val error: String? = null,
    val rateLimited: Boolean = false
)

class SpaceViewModel(
    private val repo: BiliRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SpaceUiState())
    val state: StateFlow<SpaceUiState> = _state.asStateFlow()

    private var initedMid: Long = 0
    private var job: Job? = null

    fun init(mid: Long, name: String) {
        if (initedMid == mid) return
        initedMid = mid
        repo.clearSpaceCursor(mid)
        _state.value = SpaceUiState(mid = mid, name = name, loading = true)
        loadPage(1, replace = true)
    }

    fun loadMore() {
        val s = _state.value
        if (s.mid == 0L || s.loading || !s.hasMore) return
        loadPage(s.page + 1, replace = false)
    }

    fun retry() {
        val s = _state.value
        if (s.mid == 0L) return
        val next = if (s.items.isEmpty()) 1 else s.page + 1
        loadPage(next, replace = s.items.isEmpty())
    }

    private fun loadPage(page: Int, replace: Boolean) {
        val mid = _state.value.mid
        if (mid == 0L) return
        job?.cancel()
        job = viewModelScope.launch {
            _state.update {
                it.copy(loading = true, error = null, rateLimited = false)
            }
            repo.getSpaceVideos(mid, page)
                .onSuccess { result ->
                    _state.update { s ->
                        val newItems = if (replace) result.items
                        else (s.items + result.items).distinctBy { it.bvid }
                        val noGrowth = !replace && newItems.size == s.items.size
                        s.copy(
                            items = newItems,
                            page = result.currentPage,
                            hasMore = !noGrowth && result.hasNext,
                            loading = false,
                            error = null,
                            rateLimited = false
                        )
                    }
                }
                .onFailure { e ->
                    val isRateLimited = e is BiliException.RateLimited
                    _state.update {
                        it.copy(
                            loading = false,
                            rateLimited = isRateLimited,
                            error = when {
                                isRateLimited -> "请求过于频繁，请稍后再试"
                                else -> e.message ?: "加载失败"
                            }
                        )
                    }
                }
        }
    }
}

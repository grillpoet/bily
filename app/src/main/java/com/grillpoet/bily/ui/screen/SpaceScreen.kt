package com.grillpoet.bily.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grillpoet.bily.ui.icon.AppIcons
import com.grillpoet.bily.ui.vm.SpaceViewModel
import com.grillpoet.bily.ui.widget.BackTopBar
import com.grillpoet.bily.ui.widget.InfiniteGrid
import com.grillpoet.bily.ui.widget.StateType
import com.grillpoet.bily.ui.widget.StateView
import com.grillpoet.bily.ui.widget.VideoCardView

@Composable
fun SpaceScreen(
    modifier: Modifier = Modifier,
    mid: Long,
    name: String,
    viewModel: SpaceViewModel,
    onBack: () -> Unit = {},
    onVideoClick: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(mid) { viewModel.init(mid, name) }

    Column(modifier = modifier.fillMaxSize()) {
        BackTopBar(
            title = state.name.ifBlank { name.ifBlank { "UP主" } },
            onBack = onBack
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.loading && state.items.isEmpty() -> StateView(StateType.Loading)

                state.rateLimited && state.items.isEmpty() -> StateView(
                    type = StateType.Empty,
                    icon = Icons.Rounded.Refresh,
                    title = state.error ?: "请求过于频繁",
                    subtitle = "请稍后再试"
                )

                state.error != null && state.items.isEmpty() -> StateView(
                    type = StateType.Error,
                    title = state.error ?: "",
                    onRetry = { viewModel.retry() }
                )

                state.items.isEmpty() -> StateView(
                    type = StateType.Empty,
                    icon = AppIcons.VideoLibrary,
                    title = "暂无投稿"
                )

                else -> InfiniteGrid(
                    items = state.items,
                    hasMore = state.hasMore,
                    loading = state.loading,
                    onLoadMore = { viewModel.loadMore() },
                    key = { it.bvid }
                ) { video ->
                    // 用 remember 缓存点击 lambda，避免整列重组
                    val click = remember(video.bvid, onVideoClick) {
                        { onVideoClick(video.bvid) }
                    }
                    VideoCardView(
                        video = video,
                        onClick = click
                    )
                }
            }
        }
    }
}
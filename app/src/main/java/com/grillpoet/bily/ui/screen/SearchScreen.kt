package com.grillpoet.bily.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import com.grillpoet.bily.core.normalizeCover
import com.grillpoet.bily.data.VideoCard
import com.grillpoet.bily.ui.vm.SearchViewModel
import com.grillpoet.bily.ui.widget.InfiniteGrid
import com.grillpoet.bily.ui.widget.RoundIconButton
import com.grillpoet.bily.ui.widget.StateType
import com.grillpoet.bily.ui.widget.StateView
import com.grillpoet.bily.ui.widget.VideoCardView
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel,
    onBack: () -> Unit = {},
    onVideoClick: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SearchInputBar(
            keyword = state.keyword,
            onBack = onBack,
            onSearch = viewModel::search,
            modifier = Modifier.fillMaxWidth()
        )

        SearchResultContent(
            keyword = state.keyword,
            loading = state.loading,
            error = state.error,
            searched = state.searched,
            items = state.items,
            hasMore = state.hasMore,
            onLoadMore = viewModel::loadMore,
            onVideoClick = onVideoClick,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ---------------- 输入栏 ----------------

@Composable
private fun SearchInputBar(
    keyword: String,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var input by remember { mutableStateOf(keyword) }
    LaunchedEffect(keyword) {
        if (input.isEmpty()) input = keyword
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "返回",
            onClick = onBack
        )
        Spacer(Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (input.isEmpty()) {
                    Text(
                        text = "搜索视频",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
                BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearch(input)
                            keyboard?.hide()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
            if (input.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "清空",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            input = ""
                            focusRequester.requestFocus()
                        }
                )
            }
        }
    }
}

// ---------------- 结果区 ----------------

@Composable
private fun SearchResultContent(
    keyword: String,
    loading: Boolean,
    error: String?,
    searched: Boolean,
    items: List<VideoCard>,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onVideoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val stableOnVideoClick by rememberUpdatedState(onVideoClick)
    val imageLoader = remember { context.applicationContext.imageLoader }

    // keyword 变化重建集合，等价于原来的 clear + LaunchedEffect
    val preloadedCovers = remember(keyword) { mutableSetOf<String>() }

    // 非阻塞预热：跳过首屏前 6 张（交给列表自己加载），其余后台入队
    // enqueue 内部自带调度，无需 withContext(Dispatchers.IO)
    LaunchedEffect(items) {
        if (items.isEmpty()) return@LaunchedEffect
        items.asSequence()
            .drop(6)
            .take(30)
            .map { normalizeCover(it.cover) }
            .filter { preloadedCovers.add(it) }
            .forEach { url ->
                imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(url)
                        .size(400, 225)
                        .build()
                )
            }
    }

    Box(modifier = modifier) {
        when {
            loading && items.isEmpty() -> SearchSkeleton()

            error != null && items.isEmpty() ->
                StateView(type = StateType.Error, title = error)

            searched && items.isEmpty() ->
                StateView(
                    type = StateType.Empty,
                    icon = Icons.Rounded.Search,
                    title = "没有找到相关视频",
                    subtitle = "换个关键词试试"
                )

            !searched -> StateView(
                type = StateType.Empty,
                icon = Icons.Rounded.Search,
                title = "输入关键词开始搜索"
            )

            else -> InfiniteGrid(
                items = items,
                hasMore = hasMore,
                loading = loading,
                onLoadMore = onLoadMore,
                key = { it.bvid }
            ) { video ->
                VideoCardView(
                    video = video,
                    onClick = remember(video.bvid) {
                        { stableOnVideoClick(video.bvid) }
                    }
                )
            }
        }
    }
}

// ---------------- 骨架屏 ----------------

@Composable
private fun SearchSkeleton() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false,
        modifier = Modifier.fillMaxSize()
    ) {
        items(count = 6) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
                        )
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
                        )
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
                        )
                )
            }
        }
    }
}
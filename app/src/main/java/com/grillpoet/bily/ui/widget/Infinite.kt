package com.grillpoet.bily.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> InfiniteGrid(
    items: List<T>,
    hasMore: Boolean,
    loading: Boolean,
    onLoadMore: () -> Unit,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    state: LazyGridState = rememberLazyGridState(),
    itemContent: @Composable (T) -> Unit
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            val total = state.layoutInfo.totalItemsCount
            total > 0 && last >= total - 4
        }
    }

    LaunchedEffect(shouldLoadMore, hasMore, loading) {
        if (shouldLoadMore && hasMore && !loading) onLoadMore()
    }

    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(columns),
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        modifier = modifier.fillMaxSize()
    ) {
        gridItems(
            items = items,
            key = key,
            contentType = { "item" }
        ) { item ->
            itemContent(item)
        }

        if (loading && items.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { FooterLoading() }
        }
    }
}

@Composable
private fun FooterLoading() {
    Row(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "加载中…",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}
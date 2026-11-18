package com.jorhetfield.atelier.ui.search

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.jorhetfield.atelier.R
import com.jorhetfield.atelier.designsystem.component.AtelierLoadMoreError
import com.jorhetfield.atelier.designsystem.component.AtelierLoadMoreIndicator
import com.jorhetfield.atelier.ui.components.tintedWith
import com.jorhetfield.atelier.ui.model.ArtworkUi
import com.jorhetfield.atelier.ui.model.messageRes

// The image server only serves certain widths (400 works, 300 comes back empty), and the list and the grid
// ask for the same one so switching between them reuses what is already downloaded.
internal const val RESULT_IMAGE_WIDTH = 400

private const val CARD_TINT_LIGHT = 0.16f
private const val CARD_TINT_DARK = 0.22f

// How many items before the end the next page is asked for, so it is usually there before it is needed.
private const val LOAD_MORE_THRESHOLD = 6
private const val FOOTER_KEY = "load-more"

// The tint each artwork gives its card or row: the hue of the detail page, so list and detail read as one.
@Composable
internal fun ArtworkUi.containerColor(): Color =
    MaterialTheme.colorScheme.surfaceContainerLow.tintedWith(image?.dominantColor, CARD_TINT_LIGHT, CARD_TINT_DARK)

@Composable
internal fun rememberNearEnd(
    itemCount: Int,
    lastVisibleIndex: () -> Int?,
): Boolean {
    val currentLastVisible by rememberUpdatedState(lastVisibleIndex)
    val nearEnd by remember(itemCount) {
        derivedStateOf {
            val last = currentLastVisible() ?: return@derivedStateOf false
            last >= itemCount - LOAD_MORE_THRESHOLD
        }
    }
    return nearEnd
}

// Keyed on loadMore too: when a page lands and the end is still close (short pages, tall screens),
// the next one is asked for without the user having to nudge the list.
@Composable
internal fun LoadMoreTrigger(
    nearEnd: Boolean,
    loadMore: LoadMore,
    itemCount: Int,
    enabled: Boolean,
    onLoadMore: () -> Unit,
) {
    LaunchedEffect(nearEnd, loadMore, itemCount) {
        if (enabled && nearEnd && loadMore == LoadMore.Available) onLoadMore()
    }
}

internal fun LazyGridScope.loadMoreFooter(
    loadMore: LoadMore,
    onRetry: () -> Unit,
) {
    if (loadMore !is LoadMore.Loading && loadMore !is LoadMore.Failed) return
    item(key = FOOTER_KEY, span = { GridItemSpan(maxLineSpan) }, contentType = FOOTER_KEY) {
        LoadMoreFooter(loadMore, onRetry)
    }
}

internal fun LazyStaggeredGridScope.loadMoreFooter(
    loadMore: LoadMore,
    onRetry: () -> Unit,
) {
    if (loadMore !is LoadMore.Loading && loadMore !is LoadMore.Failed) return
    item(key = FOOTER_KEY, span = StaggeredGridItemSpan.FullLine, contentType = FOOTER_KEY) {
        LoadMoreFooter(loadMore, onRetry)
    }
}

@Composable
private fun LoadMoreFooter(
    loadMore: LoadMore,
    onRetry: () -> Unit,
) {
    when (loadMore) {
        LoadMore.Loading -> {
            AtelierLoadMoreIndicator(contentDescription = stringResource(R.string.load_more_loading))
        }

        is LoadMore.Failed -> {
            AtelierLoadMoreError(
                message = stringResource(loadMore.error.messageRes),
                retryLabel = stringResource(R.string.retry),
                onRetry = onRetry,
            )
        }

        LoadMore.Available, LoadMore.Done -> {}
    }
}

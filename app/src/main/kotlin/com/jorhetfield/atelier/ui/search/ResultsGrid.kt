package com.jorhetfield.atelier.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jorhetfield.atelier.R
import com.jorhetfield.atelier.designsystem.component.ArtworkCard
import com.jorhetfield.atelier.designsystem.component.AtelierLoadMoreError
import com.jorhetfield.atelier.designsystem.component.AtelierLoadMoreIndicator
import com.jorhetfield.atelier.designsystem.motion.rememberPlayedEntrances
import com.jorhetfield.atelier.designsystem.motion.staggeredEntrance
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme
import com.jorhetfield.atelier.ui.components.ArtworkImage
import com.jorhetfield.atelier.ui.components.tintedWith
import com.jorhetfield.atelier.ui.model.ArtworkUi
import com.jorhetfield.atelier.ui.model.messageRes

private const val CARD_IMAGE_WIDTH = 400
private const val MIN_RATIO = 0.7f
private const val MAX_RATIO = 1.5f
private const val CARD_TINT_LIGHT = 0.16f
private const val CARD_TINT_DARK = 0.22f

// How many cards before the end the next page is asked for, so it is usually there before it is needed.
private const val LOAD_MORE_THRESHOLD = 6
private const val FOOTER_KEY = "load-more"

@Composable
fun ResultsGrid(
    items: List<ArtworkUi>,
    onArtworkClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    loadMore: LoadMore = LoadMore.Done,
    onLoadMore: () -> Unit = {},
) {
    val spacing = AtelierTheme.spacing
    val played = rememberPlayedEntrances()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val baseContainer = MaterialTheme.colorScheme.surfaceContainerLow

    val gridState = rememberLazyStaggeredGridState()
    val itemCount = items.size
    val nearEnd by remember(itemCount) {
        derivedStateOf {
            val lastVisible =
                gridState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index ?: return@derivedStateOf false
            lastVisible >= itemCount - LOAD_MORE_THRESHOLD
        }
    }
    // Keyed on loadMore too: when a page lands and the end is still close (short pages, tall screens),
    // the next one is asked for without the user having to nudge the list.
    LaunchedEffect(nearEnd, loadMore, itemCount) {
        if (interactive && nearEnd && loadMore == LoadMore.Available) onLoadMore()
    }

    LazyVerticalStaggeredGrid(
        state = gridState,
        columns = StaggeredGridCells.Adaptive(168.dp),
        modifier = modifier,
        contentPadding =
            PaddingValues(
                start = spacing.lg,
                end = spacing.lg,
                top = spacing.sm,
                bottom = spacing.xl + bottomInset,
            ),
        verticalItemSpacing = spacing.md,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        userScrollEnabled = interactive,
    ) {
        itemsIndexed(items, key = { _, artwork -> artwork.id }) { index, artwork ->
            ArtworkCard(
                title = artwork.title,
                subtitle = artwork.artist,
                onClick = { onArtworkClick(artwork.id) },
                imageAspectRatio = (artwork.image?.aspectRatio ?: 1f).coerceIn(MIN_RATIO, MAX_RATIO),
                containerColor =
                    baseContainer.tintedWith(artwork.image?.dominantColor, CARD_TINT_LIGHT, CARD_TINT_DARK),
                modifier = Modifier.staggeredEntrance(artwork.id, index, played).animateItem(),
                image = { ArtworkImage(artwork.image, width = CARD_IMAGE_WIDTH) },
            )
        }
        when (loadMore) {
            LoadMore.Loading -> {
                item(key = FOOTER_KEY, span = StaggeredGridItemSpan.FullLine, contentType = FOOTER_KEY) {
                    AtelierLoadMoreIndicator(contentDescription = stringResource(R.string.load_more_loading))
                }
            }

            is LoadMore.Failed -> {
                item(key = FOOTER_KEY, span = StaggeredGridItemSpan.FullLine, contentType = FOOTER_KEY) {
                    AtelierLoadMoreError(
                        message = stringResource(loadMore.error.messageRes),
                        retryLabel = stringResource(R.string.retry),
                        onRetry = onLoadMore,
                    )
                }
            }

            LoadMore.Available, LoadMore.Done -> {}
        }
    }
}

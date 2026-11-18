package com.jorhetfield.atelier.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jorhetfield.atelier.designsystem.component.ArtworkCard
import com.jorhetfield.atelier.designsystem.motion.rememberPlayedEntrances
import com.jorhetfield.atelier.designsystem.motion.staggeredEntrance
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme
import com.jorhetfield.atelier.ui.components.ArtworkImage
import com.jorhetfield.atelier.ui.model.ArtworkUi

private const val MIN_RATIO = 0.7f
private const val MAX_RATIO = 1.5f

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

    val gridState = rememberLazyStaggeredGridState()
    val nearEnd =
        rememberNearEnd(items.size) {
            gridState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }
    LoadMoreTrigger(nearEnd, loadMore, items.size, interactive, onLoadMore)

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
                containerColor = artwork.containerColor(),
                modifier = Modifier.staggeredEntrance(artwork.id, index, played).animateItem(),
                image = { ArtworkImage(artwork.image, width = RESULT_IMAGE_WIDTH) },
            )
        }
        loadMoreFooter(loadMore, onLoadMore)
    }
}

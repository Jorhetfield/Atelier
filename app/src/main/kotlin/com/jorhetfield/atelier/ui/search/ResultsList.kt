package com.jorhetfield.atelier.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jorhetfield.atelier.designsystem.component.ArtworkListItem
import com.jorhetfield.atelier.designsystem.motion.rememberPlayedEntrances
import com.jorhetfield.atelier.designsystem.motion.staggeredEntrance
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme
import com.jorhetfield.atelier.ui.components.ArtworkImage
import com.jorhetfield.atelier.ui.model.ArtworkUi

// A row needs about this much room to read well. Narrower windows get one column of rows, and wider ones
// (a phone on its side, a tablet) fit two or more, so the list always uses the whole width.
private val MinRowWidth = 340.dp

@Composable
fun ResultsList(
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

    val listState = rememberLazyGridState()
    val nearEnd =
        rememberNearEnd(items.size) {
            listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }
    LoadMoreTrigger(nearEnd, loadMore, items.size, interactive, onLoadMore)

    LazyVerticalGrid(
        state = listState,
        columns = GridCells.Adaptive(MinRowWidth),
        modifier = modifier,
        contentPadding =
            PaddingValues(
                start = spacing.lg,
                end = spacing.lg,
                top = spacing.sm,
                bottom = spacing.xl + bottomInset,
            ),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        userScrollEnabled = interactive,
    ) {
        itemsIndexed(items, key = { _, artwork -> artwork.id }) { index, artwork ->
            ArtworkListItem(
                title = artwork.title,
                subtitle = artwork.artist,
                onClick = { onArtworkClick(artwork.id) },
                containerColor = artwork.containerColor(),
                modifier = Modifier.fillMaxWidth().staggeredEntrance(artwork.id, index, played).animateItem(),
                image = { ArtworkImage(artwork.image, width = RESULT_IMAGE_WIDTH) },
            )
        }
        loadMoreFooter(loadMore, onLoadMore)
    }
}

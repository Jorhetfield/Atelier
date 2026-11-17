package com.jorhetfield.atelier.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jorhetfield.atelier.designsystem.component.ArtworkCard
import com.jorhetfield.atelier.designsystem.motion.rememberPlayedEntrances
import com.jorhetfield.atelier.designsystem.motion.staggeredEntrance
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme
import com.jorhetfield.atelier.ui.components.ArtworkImage
import com.jorhetfield.atelier.ui.components.tintedWith
import com.jorhetfield.atelier.ui.model.ArtworkUi

private const val CARD_IMAGE_WIDTH = 400
private const val MIN_RATIO = 0.7f
private const val MAX_RATIO = 1.5f
private const val CARD_TINT_LIGHT = 0.16f
private const val CARD_TINT_DARK = 0.22f

@Composable
fun ResultsGrid(
    items: List<ArtworkUi>,
    onArtworkClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
) {
    val spacing = AtelierTheme.spacing
    val played = rememberPlayedEntrances()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val baseContainer = MaterialTheme.colorScheme.surfaceContainerLow

    LazyVerticalStaggeredGrid(
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
    }
}

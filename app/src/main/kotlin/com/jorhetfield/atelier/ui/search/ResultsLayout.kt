package com.jorhetfield.atelier.ui.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jorhetfield.atelier.R
import com.jorhetfield.atelier.designsystem.component.AtelierLayoutToggle
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme
import com.jorhetfield.atelier.ui.model.ArtworkUi

// A list is what the brief asks for and the default; the grid shows more artwork per screen.
enum class ResultsLayout { List, Grid }

@Composable
fun ResultsLayoutToggle(
    layout: ResultsLayout,
    onLayoutChange: (ResultsLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    AtelierLayoutToggle(
        gridSelected = layout == ResultsLayout.Grid,
        onGridSelectedChange = { grid -> onLayoutChange(if (grid) ResultsLayout.Grid else ResultsLayout.List) },
        listLabel = stringResource(R.string.layout_list),
        gridLabel = stringResource(R.string.layout_grid),
        modifier = modifier.padding(horizontal = AtelierTheme.spacing.lg, vertical = AtelierTheme.spacing.sm),
    )
}

// Switching crossfades between the two, so the change reads as one gesture and not a jump.
@Composable
fun ArtworkResults(
    items: List<ArtworkUi>,
    layout: ResultsLayout,
    onArtworkClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    loadMore: LoadMore = LoadMore.Done,
    onLoadMore: () -> Unit = {},
) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = layout,
        transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
        modifier = modifier,
        label = "results layout",
    ) { current ->
        when (current) {
            ResultsLayout.List -> {
                ResultsList(
                    items,
                    onArtworkClick,
                    interactive = interactive,
                    loadMore = loadMore,
                    onLoadMore = onLoadMore,
                )
            }

            ResultsLayout.Grid -> {
                ResultsGrid(
                    items,
                    onArtworkClick,
                    interactive = interactive,
                    loadMore = loadMore,
                    onLoadMore = onLoadMore,
                )
            }
        }
    }
}

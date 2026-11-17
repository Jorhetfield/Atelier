package com.jorhetfield.atelier.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jorhetfield.atelier.R
import com.jorhetfield.atelier.designsystem.component.AtelierCarousel
import com.jorhetfield.atelier.designsystem.component.AtelierCarouselCard
import com.jorhetfield.atelier.designsystem.component.AtelierCarouselPlaceholder
import com.jorhetfield.atelier.designsystem.component.AtelierEmptyState
import com.jorhetfield.atelier.designsystem.component.AtelierErrorState
import com.jorhetfield.atelier.designsystem.component.AtelierSectionHeader
import com.jorhetfield.atelier.designsystem.motion.rememberPlayedEntrances
import com.jorhetfield.atelier.designsystem.motion.staggeredEntrance
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme
import com.jorhetfield.atelier.ui.components.ArtworkImage
import com.jorhetfield.atelier.ui.model.messageRes
import com.jorhetfield.atelier.ui.model.titleRes

private const val CAROUSEL_IMAGE_WIDTH = 600
private const val PLACEHOLDER_COUNT = 4

@Composable
fun HomeContent(
    state: HomeUiState,
    onArtworkClick: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val failure = state.failure
    val shelves = state.visibleShelves

    when {
        failure != null -> {
            Centered(modifier) {
                AtelierErrorState(
                    title = stringResource(failure.titleRes),
                    message = stringResource(failure.messageRes),
                    retryLabel = stringResource(R.string.retry),
                    onRetry = onRetry,
                )
            }
        }

        shelves.isEmpty() -> {
            Centered(modifier) {
                AtelierEmptyState(
                    title = stringResource(R.string.idle_title),
                    message = stringResource(R.string.idle_message),
                )
            }
        }

        else -> {
            Shelves(shelves = shelves, onArtworkClick = onArtworkClick, modifier = modifier)
        }
    }
}

@Composable
private fun Shelves(
    shelves: List<ShelfUi>,
    onArtworkClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = AtelierTheme.spacing
    val played = rememberPlayedEntrances()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = spacing.sm, bottom = spacing.xl + bottomInset),
        verticalArrangement = Arrangement.spacedBy(spacing.xl),
    ) {
        itemsIndexed(shelves, key = { _, shelf -> shelf.shelf }) { index, shelf ->
            Column(modifier = Modifier.staggeredEntrance(key = shelf.shelf, index = index, played = played)) {
                AtelierSectionHeader(
                    title = stringResource(shelf.shelf.title),
                    subtitle = stringResource(shelf.shelf.subtitle),
                )
                Spacer(modifier = Modifier.height(spacing.md))
                ShelfCarousel(content = shelf.content, onArtworkClick = onArtworkClick)
            }
        }
    }
}

@Composable
private fun ShelfCarousel(
    content: ShelfContent,
    onArtworkClick: (Int) -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = content,
        contentKey = { it::class },
        transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
        label = "shelf content",
    ) { current ->
        when (current) {
            is ShelfContent.Loaded -> {
                AtelierCarousel(itemCount = current.items.size) { index ->
                    val artwork = current.items[index]
                    AtelierCarouselCard(
                        title = artwork.title,
                        subtitle = artwork.artist,
                        onClick = { onArtworkClick(artwork.id) },
                        image = { ArtworkImage(artwork.image, width = CAROUSEL_IMAGE_WIDTH) },
                    )
                }
            }

            else -> {
                AtelierCarousel(itemCount = PLACEHOLDER_COUNT) { AtelierCarouselPlaceholder() }
            }
        }
    }
}

@Composable
private fun Centered(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

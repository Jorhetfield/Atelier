package com.jorhetfield.atelier.ui.detail

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jorhetfield.atelier.R
import com.jorhetfield.atelier.designsystem.component.AtelierDetailRow
import com.jorhetfield.atelier.designsystem.component.AtelierErrorState
import com.jorhetfield.atelier.designsystem.component.AtelierLoadingState
import com.jorhetfield.atelier.designsystem.motion.rememberPlayedEntrances
import com.jorhetfield.atelier.designsystem.motion.staggeredEntrance
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme
import com.jorhetfield.atelier.ui.components.ArtworkImage
import com.jorhetfield.atelier.ui.components.DETAIL_IMAGE_WIDTH
import com.jorhetfield.atelier.ui.components.LightSystemBarIcons
import com.jorhetfield.atelier.ui.components.tintedWith
import com.jorhetfield.atelier.ui.model.ArtworkDetailUi
import com.jorhetfield.atelier.ui.model.ArtworkImageUi
import com.jorhetfield.atelier.ui.model.messageRes
import com.jorhetfield.atelier.ui.model.titleRes

private const val HERO_HEIGHT_FRACTION = 0.5f
private const val PARALLAX_FACTOR = 0.5f
private const val PARALLAX_OVERSCAN = 1.25f
private const val TINT_LIGHT = 0.16f
private const val TINT_DARK = 0.24f

@Composable
fun DetailScreen(
    artworkId: Int,
    onBack: () -> Unit,
    onViewArtwork: (ArtworkImageUi) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel =
        hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(artworkId) },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreenContent(
        state = state,
        onBack = onBack,
        onViewArtwork = onViewArtwork,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
fun DetailScreenContent(
    state: DetailUiState,
    onBack: () -> Unit,
    onViewArtwork: (ArtworkImageUi) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val surface = MaterialTheme.colorScheme.surface
    val dominantColor = (state as? DetailUiState.Success)?.artwork?.dominantColor
    val background by animateColorAsState(
        targetValue = surface.tintedWith(dominantColor, TINT_LIGHT, TINT_DARK),
        animationSpec = motion.defaultEffectsSpec(),
        label = "detail tint",
    )

    // Surface, not a bare Box: it sets the content color, so text is readable in dark theme too.
    Surface(
        modifier = modifier.fillMaxSize(),
        color = background,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = state,
                contentKey = { it::class },
                transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
                modifier = Modifier.fillMaxSize(),
                label = "detail content",
            ) { current ->
                when (current) {
                    DetailUiState.Loading -> {
                        AtelierLoadingState(contentDescription = stringResource(R.string.detail_loading))
                    }

                    is DetailUiState.Failure -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            AtelierErrorState(
                                title = stringResource(current.error.titleRes),
                                message = stringResource(current.error.messageRes),
                                retryLabel = stringResource(R.string.retry),
                                onRetry = onRetry,
                            )
                        }
                    }

                    is DetailUiState.Success -> {
                        ArtworkDetail(current.artwork, onViewArtwork)
                    }
                }
            }

            FilledTonalIconButton(
                onClick = onBack,
                colors =
                    IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = surface.copy(alpha = 0.78f),
                    ),
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Start),
                        ).padding(AtelierTheme.spacing.md),
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.detail_back))
            }
        }
    }
}

@Composable
private fun ArtworkDetail(
    artwork: ArtworkDetailUi,
    onViewArtwork: (ArtworkImageUi) -> Unit,
) {
    val spacing = AtelierTheme.spacing
    val listState = rememberLazyListState()
    val played = rememberPlayedEntrances()
    val statusBarPx = WindowInsets.statusBars.getTop(LocalDensity.current)
    val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val rows = artwork.rows()

    val pictureUnderStatusBar by remember {
        derivedStateOf {
            val first = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            first == null || (first.index == 0 && first.offset + first.size > statusBarPx)
        }
    }
    LightSystemBarIcons(statusBars = pictureUnderStatusBar)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = spacing.xl + navigationBar),
    ) {
        item(key = "hero") {
            Hero(
                artwork = artwork,
                listState = listState,
                onViewArtwork = onViewArtwork,
                modifier = Modifier.fillParentMaxHeight(HERO_HEIGHT_FRACTION),
            )
        }
        item(key = "title") {
            Column(
                modifier = Modifier.padding(horizontal = spacing.lg).padding(top = spacing.xl, bottom = spacing.lg),
            ) {
                Text(text = artwork.title, style = MaterialTheme.typography.headlineMedium)
                artwork.artist?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.xs),
                    )
                }
            }
        }
        itemsIndexed(rows, key = { _, row -> row.first }) { index, (label, value) ->
            AtelierDetailRow(
                label = stringResource(label),
                value = value,
                modifier =
                    Modifier
                        .padding(horizontal = spacing.lg)
                        .staggeredEntrance(key = artwork.id to label, index = index, played = played),
            )
        }
    }
}

// Edge to edge on the top half. The frame scrolls with the list while the picture inside lags behind
// it, which gives the depth; the overscan keeps its edges from showing as it shifts.
@Composable
private fun Hero(
    artwork: ArtworkDetailUi,
    listState: LazyListState,
    onViewArtwork: (ArtworkImageUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomRounded =
        MaterialTheme.shapes.extraLarge.copy(topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp))
    val image = artwork.image
    val openViewer = stringResource(R.string.detail_view_full)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(bottomRounded)
                .then(
                    if (image !=
                        null
                    ) {
                        Modifier.clickable(onClickLabel = openViewer) { onViewArtwork(image) }
                    } else {
                        Modifier
                    },
                ),
    ) {
        ArtworkImage(
            image = image,
            width = DETAIL_IMAGE_WIDTH,
            modifier =
                Modifier.graphicsLayer {
                    val offset = if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset else 0
                    translationY = offset * PARALLAX_FACTOR
                    scaleX = PARALLAX_OVERSCAN
                    scaleY = PARALLAX_OVERSCAN
                },
        )
        // Darkens the top so the status bar icons and the back button stay readable over any picture.
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(statusBar + 96.dp)
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent))),
        )
        if (image != null) {
            FilledTonalButton(
                onClick = { onViewArtwork(image) },
                colors =
                    ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                    ),
                modifier = Modifier.align(Alignment.BottomEnd).padding(AtelierTheme.spacing.lg),
            ) {
                Text(openViewer)
            }
        }
    }
}

@Composable
private fun ArtworkDetailUi.rows(): List<Pair<Int, String>> {
    val publicDomain = stringResource(if (isPublicDomain) R.string.detail_yes else R.string.detail_no)
    return listOfNotNull(
        date.row(R.string.detail_date),
        medium.row(R.string.detail_medium),
        origin.row(R.string.detail_origin),
        dimensions.row(R.string.detail_dimensions),
        classification.row(R.string.detail_classification),
        style.row(R.string.detail_style),
        R.string.detail_public_domain to publicDomain,
    )
}

private fun String?.row(
    @StringRes label: Int,
): Pair<Int, String>? = this?.let { label to it }

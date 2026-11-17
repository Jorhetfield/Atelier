package com.jorhetfield.atelier.ui.detail

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jorhetfield.atelier.R
import com.jorhetfield.atelier.designsystem.component.AtelierDetailRow
import com.jorhetfield.atelier.designsystem.component.AtelierErrorState
import com.jorhetfield.atelier.designsystem.component.AtelierLoadingState
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme
import com.jorhetfield.atelier.ui.components.ArtworkImage
import com.jorhetfield.atelier.ui.model.ArtworkDetailUi
import com.jorhetfield.atelier.ui.model.messageRes
import com.jorhetfield.atelier.ui.model.titleRes

private const val HERO_IMAGE_WIDTH = 843
private const val MIN_RATIO = 0.6f
private const val MAX_RATIO = 1.6f

@Composable
fun DetailScreen(
    artworkId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel =
        hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
            creationCallback = { factory -> factory.create(artworkId) },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreenContent(state = state, onBack = onBack, onRetry = viewModel::retry, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreenContent(
    state: DetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = state,
            contentKey = { it::class },
            transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
            modifier = Modifier.fillMaxSize().padding(padding),
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
                    ArtworkDetail(current.artwork)
                }
            }
        }
    }
}

@Composable
private fun ArtworkDetail(artwork: ArtworkDetailUi) {
    val spacing = AtelierTheme.spacing
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val rows = artwork.rows()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                start = spacing.lg,
                end = spacing.lg,
                top = spacing.sm,
                bottom = spacing.xl + bottomInset,
            ),
    ) {
        item {
            ArtworkImage(
                image = artwork.image,
                width = HERO_IMAGE_WIDTH,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio((artwork.image?.aspectRatio ?: 1f).coerceIn(MIN_RATIO, MAX_RATIO))
                        .clip(MaterialTheme.shapes.extraLarge),
            )
        }
        item {
            Column(modifier = Modifier.padding(vertical = spacing.xl)) {
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
        items(rows) { (label, value) ->
            AtelierDetailRow(label = stringResource(label), value = value)
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

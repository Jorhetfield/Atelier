package com.jorhetfield.atelier.ui.shelf

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jorhetfield.atelier.R
import com.jorhetfield.atelier.designsystem.component.AtelierEmptyState
import com.jorhetfield.atelier.designsystem.component.AtelierErrorState
import com.jorhetfield.atelier.designsystem.component.AtelierLoadingState
import com.jorhetfield.atelier.ui.home.HomeShelf
import com.jorhetfield.atelier.ui.model.messageRes
import com.jorhetfield.atelier.ui.model.titleRes
import com.jorhetfield.atelier.ui.search.ResultsGrid

@Composable
fun ShelfScreen(
    shelf: HomeShelf,
    onBack: () -> Unit,
    onArtworkClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShelfViewModel =
        hiltViewModel<ShelfViewModel, ShelfViewModel.Factory>(
            creationCallback = { factory -> factory.create(shelf) },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ShelfScreenContent(
        title = stringResource(shelf.title),
        subtitle = stringResource(shelf.subtitle),
        state = state,
        onBack = onBack,
        onArtworkClick = onArtworkClick,
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadMore,
        modifier = modifier,
    )
}

// Same collapsing title as the search screen, so the two feel like one app.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShelfScreenContent(
    title: String,
    subtitle: String,
    state: ShelfUiState,
    onBack: () -> Unit,
    onArtworkClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(title) },
                subtitle = { Text(subtitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                    ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = state,
            contentKey = { it::class },
            transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
            modifier = Modifier.fillMaxSize().padding(padding),
            label = "shelf content",
        ) { current ->
            when (current) {
                ShelfUiState.Loading -> {
                    AtelierLoadingState(contentDescription = stringResource(R.string.search_loading))
                }

                ShelfUiState.Empty -> {
                    Centered {
                        AtelierEmptyState(
                            title = stringResource(R.string.shelf_empty_title),
                            message = stringResource(R.string.shelf_empty_message),
                        )
                    }
                }

                is ShelfUiState.Failure -> {
                    Centered {
                        AtelierErrorState(
                            title = stringResource(current.error.titleRes),
                            message = stringResource(current.error.messageRes),
                            retryLabel = stringResource(R.string.retry),
                            onRetry = onRetry,
                        )
                    }
                }

                is ShelfUiState.Content -> {
                    ResultsGrid(
                        items = current.items,
                        onArtworkClick = onArtworkClick,
                        loadMore = current.loadMore,
                        onLoadMore = onLoadMore,
                    )
                }
            }
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

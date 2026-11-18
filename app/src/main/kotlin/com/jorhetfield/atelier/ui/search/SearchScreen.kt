package com.jorhetfield.atelier.ui.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jorhetfield.atelier.R
import com.jorhetfield.atelier.designsystem.component.AtelierEmptyState
import com.jorhetfield.atelier.designsystem.component.AtelierErrorState
import com.jorhetfield.atelier.designsystem.component.AtelierLoadingState
import com.jorhetfield.atelier.designsystem.component.AtelierSearchField
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme
import com.jorhetfield.atelier.ui.home.HomeContent
import com.jorhetfield.atelier.ui.home.HomeShelf
import com.jorhetfield.atelier.ui.home.HomeUiState
import com.jorhetfield.atelier.ui.home.HomeViewModel
import com.jorhetfield.atelier.ui.model.messageRes
import com.jorhetfield.atelier.ui.model.titleRes

@Composable
fun SearchScreen(
    onArtworkClick: (Int) -> Unit,
    onShelfClick: (HomeShelf) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val home by homeViewModel.uiState.collectAsStateWithLifecycle()
    SearchScreenContent(
        state = state,
        home = home,
        onQueryChange = viewModel::onQueryChange,
        onSubmit = viewModel::onSubmit,
        onRetry = viewModel::retry,
        onHomeRetry = homeViewModel::retry,
        onLoadMore = viewModel::loadMore,
        onArtworkClick = onArtworkClick,
        onShelfClick = onShelfClick,
        modifier = modifier,
    )
}

// LargeFlexibleTopAppBar is the expressive collapsing title bar; it graduated from experimental in
// Material 3 1.5, but the opt-in is kept for the scroll behavior API.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchScreenContent(
    state: SearchUiState,
    home: HomeUiState,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onHomeRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onArtworkClick: (Int) -> Unit,
    onShelfClick: (HomeShelf) -> Unit,
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
                title = { Text(stringResource(R.string.app_name)) },
                subtitle = { Text(stringResource(R.string.search_tagline)) },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                    ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchField(query = state.query, onQueryChange = onQueryChange, onSubmit = onSubmit)

            AnimatedContent(
                targetState = state.content,
                contentKey = { it::class },
                transitionSpec = {
                    fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
                },
                modifier = Modifier.weight(1f).imePadding(),
                label = "search content",
            ) { content ->
                when (content) {
                    SearchContent.Idle -> {
                        HomeContent(
                            state = home,
                            onArtworkClick = onArtworkClick,
                            onShelfClick = onShelfClick,
                            onRetry = onHomeRetry,
                        )
                    }

                    is SearchContent.Loading -> {
                        LoadingContent(content, onArtworkClick)
                    }

                    is SearchContent.Results -> {
                        ResultsGrid(
                            items = content.items,
                            onArtworkClick = onArtworkClick,
                            loadMore = content.loadMore,
                            onLoadMore = onLoadMore,
                        )
                    }

                    is SearchContent.NoResults -> {
                        Centered {
                            AtelierEmptyState(
                                title = stringResource(R.string.no_results_title, content.query),
                                message = stringResource(R.string.no_results_message),
                            )
                        }
                    }

                    is SearchContent.Failure -> {
                        Centered {
                            AtelierErrorState(
                                title = stringResource(content.error.titleRes),
                                message = stringResource(content.error.messageRes),
                                retryLabel = stringResource(R.string.retry),
                                onRetry = onRetry,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    AtelierSearchField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = stringResource(R.string.search_placeholder),
        clearContentDescription = stringResource(R.string.search_clear),
        modifier = Modifier.padding(horizontal = AtelierTheme.spacing.lg, vertical = AtelierTheme.spacing.sm),
        onSearch = {
            onSubmit()
            keyboard?.hide()
        },
    )
}

// The previous results are only decoration while loading: blurred, dimmed, hidden from
// accessibility services and shielded from touches, so they can't be mistaken for current results.
@Composable
private fun LoadingContent(
    content: SearchContent.Loading,
    onArtworkClick: (Int) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (content.previous.isNotEmpty()) {
            ResultsGrid(
                items = content.previous,
                onArtworkClick = onArtworkClick,
                interactive = false,
                modifier = Modifier.blur(16.dp).alpha(0.5f).clearAndSetSemantics {},
            )
            Box(modifier = Modifier.matchParentSize().pointerInput(Unit) { detectTapGestures { } })
        }
        AtelierLoadingState(contentDescription = stringResource(R.string.search_loading))
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

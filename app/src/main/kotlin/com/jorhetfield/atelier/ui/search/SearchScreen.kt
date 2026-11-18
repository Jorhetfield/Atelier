package com.jorhetfield.atelier.ui.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
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
    layout: ResultsLayout,
    onLayoutChange: (ResultsLayout) -> Unit,
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
        layout = layout,
        onLayoutChange = onLayoutChange,
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
    layout: ResultsLayout,
    onLayoutChange: (ResultsLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // The field owns its text. Reading it back through the view model's flows (combine, stateIn, collect)
    // delays it by a frame or more, and when several characters arrive at once (paste, dictation, glide
    // typing) the field repaints stale text and drops or reorders letters. The view model still gets every
    // change; its query only seeds the field.
    var query by rememberSaveable { mutableStateOf(state.query) }
    // A recreated process restores the field's text but starts the view model empty.
    LaunchedEffect(Unit) { if (query != state.query) onQueryChange(query) }

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
            SearchField(
                query = query,
                onQueryChange = {
                    query = it
                    onQueryChange(it)
                },
                onSubmit = onSubmit,
            )

            // Only worth showing when there is a list to lay out. It stays through the loading of the next
            // search, so the row does not jump out and back in between two searches.
            val content = state.content
            AnimatedVisibility(
                visible = content is SearchContent.Results || content is SearchContent.Loading,
                enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
            ) {
                ResultsLayoutToggle(layout = layout, onLayoutChange = onLayoutChange)
            }

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

                    SearchContent.Loading -> {
                        AtelierLoadingState(contentDescription = stringResource(R.string.search_loading))
                    }

                    is SearchContent.Results -> {
                        ArtworkResults(
                            items = content.items,
                            layout = layout,
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

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

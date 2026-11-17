package com.jorhetfield.atelier.ui.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
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
import com.jorhetfield.atelier.ui.model.messageRes
import com.jorhetfield.atelier.ui.model.titleRes

@Composable
fun SearchScreen(
    onArtworkClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreenContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSubmit = viewModel::onSubmit,
        onRetry = viewModel::retry,
        onArtworkClick = onArtworkClick,
        modifier = modifier,
    )
}

@Composable
fun SearchScreenContent(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onArtworkClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                ),
    ) {
        SearchHeader(query = state.query, onQueryChange = onQueryChange, onSubmit = onSubmit)

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
                    Centered {
                        AtelierEmptyState(
                            title = stringResource(R.string.idle_title),
                            message = stringResource(R.string.idle_message),
                        )
                    }
                }

                is SearchContent.Loading -> {
                    LoadingContent(content, onArtworkClick)
                }

                is SearchContent.Results -> {
                    ResultsGrid(items = content.items, onArtworkClick = onArtworkClick)
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

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val spacing = AtelierTheme.spacing
    val keyboard = LocalSoftwareKeyboardController.current
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg)
                .padding(top = spacing.xl, bottom = spacing.md),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            text = stringResource(R.string.search_tagline),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = spacing.lg),
        )
        AtelierSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(R.string.search_placeholder),
            clearContentDescription = stringResource(R.string.search_clear),
            onSearch = {
                onSubmit()
                keyboard?.hide()
            },
        )
    }
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

package com.jorhetfield.atelier.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.jorhetfield.atelier.ui.detail.DetailScreen
import com.jorhetfield.atelier.ui.model.ArtworkImageUi
import com.jorhetfield.atelier.ui.search.SearchScreen
import com.jorhetfield.atelier.ui.viewer.ArtworkViewerScreen

@Composable
fun AtelierNavHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(SearchRoute)
    val motion = MaterialTheme.motionScheme

    // The detail rises from below over the search and leaves the same way. While it leaves it must
    // stay on top, hence the negative z-index on the screen that is coming back.
    val rise =
        slideInVertically(motion.defaultSpatialSpec()) { it } + fadeIn(motion.defaultEffectsSpec()) togetherWith
            fadeOut(motion.defaultEffectsSpec(), targetAlpha = 0.5f)
    val sink =
        (
            EnterTransition.None togetherWith
                (slideOutVertically(motion.defaultSpatialSpec()) { it } + fadeOut(motion.fastEffectsSpec()))
        ).apply {
            targetContentZIndex = -1f
        }

    // The viewer opens like a lens: it grows slightly while it fades in over the detail, and shrinks back out.
    val zoomIn =
        fadeIn(motion.defaultEffectsSpec()) + scaleIn(motion.defaultSpatialSpec(), initialScale = 0.92f) togetherWith
            fadeOut(motion.defaultEffectsSpec(), targetAlpha = 0.6f)
    val zoomOut =
        (
            EnterTransition.None togetherWith
                (fadeOut(motion.defaultEffectsSpec()) + scaleOut(motion.defaultSpatialSpec(), targetScale = 0.92f))
        ).apply {
            targetContentZIndex = -1f
        }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        // The view model store decorator gives every entry its own ViewModels, so each artwork
        // opened gets a fresh DetailViewModel built from its key.
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        transitionSpec = { rise },
        popTransitionSpec = { sink },
        predictivePopTransitionSpec = { sink },
        entryProvider =
            entryProvider {
                entry<SearchRoute> {
                    SearchScreen(
                        // A double tap must not stack two detail screens.
                        onArtworkClick = { id ->
                            if (backStack.lastOrNull() is SearchRoute) backStack.add(DetailRoute(id))
                        },
                    )
                }
                entry<DetailRoute> { key ->
                    DetailScreen(
                        artworkId = key.artworkId,
                        onBack = { backStack.removeLastOrNull() },
                        onViewArtwork = { image ->
                            if (backStack.lastOrNull() is DetailRoute) {
                                backStack.add(
                                    ViewerRoute(
                                        image.id,
                                        image.contentDescription,
                                        image.aspectRatio,
                                        image.sourceWidth,
                                    ),
                                )
                            }
                        },
                    )
                }
                entry<ViewerRoute>(
                    metadata =
                        metadata {
                            put(NavDisplay.TransitionKey) { zoomIn }
                            put(NavDisplay.PopTransitionKey) { zoomOut }
                            put(NavDisplay.PredictivePopTransitionKey) { zoomOut }
                        },
                ) { key ->
                    ArtworkViewerScreen(
                        image =
                            ArtworkImageUi(
                                id = key.imageId,
                                contentDescription = key.contentDescription,
                                placeholder = null,
                                aspectRatio = key.aspectRatio,
                                sourceWidth = key.sourceWidth,
                            ),
                        onClose = { backStack.removeLastOrNull() },
                    )
                }
            },
    )
}

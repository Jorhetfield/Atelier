package com.jorhetfield.atelier.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.jorhetfield.atelier.ui.detail.DetailScreen
import com.jorhetfield.atelier.ui.search.SearchScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AtelierNavHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(SearchRoute)

    SharedTransitionLayout(modifier = modifier) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            sharedTransitionScope = this,
            // The view model store decorator gives every entry its own ViewModels, so each artwork
            // opened gets a fresh DetailViewModel built from its key.
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
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
                        DetailScreen(artworkId = key.artworkId, onBack = { backStack.removeLastOrNull() })
                    }
                },
        )
    }
}

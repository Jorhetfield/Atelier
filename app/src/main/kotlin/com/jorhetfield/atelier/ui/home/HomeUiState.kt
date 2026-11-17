package com.jorhetfield.atelier.ui.home

import com.jorhetfield.atelier.ui.model.ArtworkUi
import com.jorhetfield.atelier.ui.model.ErrorKind

data class HomeUiState(
    val shelves: List<ShelfUi>,
) {
    // A shelf that failed or came back empty is left out: a missing row is better than an error in the middle.
    val visibleShelves: List<ShelfUi>
        get() =
            shelves.filter {
                when (val content = it.content) {
                    ShelfContent.Loading -> true
                    is ShelfContent.Loaded -> content.items.isNotEmpty()
                    is ShelfContent.Failed -> false
                }
            }

    // Only when every shelf failed is there nothing to show, and then the whole screen says why.
    val failure: ErrorKind?
        get() =
            shelves
                .map { it.content }
                .takeIf { contents -> contents.isNotEmpty() && contents.all { it is ShelfContent.Failed } }
                ?.filterIsInstance<ShelfContent.Failed>()
                ?.first()
                ?.error
}

data class ShelfUi(
    val shelf: HomeShelf,
    val content: ShelfContent,
)

sealed interface ShelfContent {
    data object Loading : ShelfContent

    data class Loaded(
        val items: List<ArtworkUi>,
    ) : ShelfContent

    data class Failed(
        val error: ErrorKind,
    ) : ShelfContent
}

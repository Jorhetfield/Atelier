package com.jorhetfield.atelier.ui.shelf

import com.jorhetfield.atelier.ui.model.ArtworkUi
import com.jorhetfield.atelier.ui.model.ErrorKind
import com.jorhetfield.atelier.ui.search.LoadMore

sealed interface ShelfUiState {
    data object Loading : ShelfUiState

    data object Empty : ShelfUiState

    data class Failure(
        val error: ErrorKind,
    ) : ShelfUiState

    data class Content(
        val items: List<ArtworkUi>,
        val loadMore: LoadMore = LoadMore.Done,
    ) : ShelfUiState
}

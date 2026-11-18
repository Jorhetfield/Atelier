package com.jorhetfield.atelier.ui.search

import com.jorhetfield.atelier.ui.model.ArtworkUi
import com.jorhetfield.atelier.ui.model.ErrorKind

data class SearchUiState(
    val query: String = "",
    val content: SearchContent = SearchContent.Idle,
)

sealed interface SearchContent {
    data object Idle : SearchContent

    // Nothing of the previous search stays on screen while the next one loads.
    data object Loading : SearchContent

    data class Results(
        val items: List<ArtworkUi>,
        val loadMore: LoadMore = LoadMore.Done,
    ) : SearchContent

    data class NoResults(
        val query: String,
    ) : SearchContent

    data class Failure(
        val error: ErrorKind,
    ) : SearchContent
}

// What the end of a results list is doing. A failed page keeps everything already shown.
sealed interface LoadMore {
    data object Done : LoadMore

    data object Available : LoadMore

    data object Loading : LoadMore

    data class Failed(
        val error: ErrorKind,
    ) : LoadMore
}

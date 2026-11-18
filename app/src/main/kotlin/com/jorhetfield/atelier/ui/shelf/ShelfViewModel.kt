package com.jorhetfield.atelier.ui.shelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.model.SearchPage
import com.jorhetfield.atelier.domain.usecase.GetHighlightsUseCase
import com.jorhetfield.atelier.domain.usecase.SearchArtworksUseCase
import com.jorhetfield.atelier.ui.home.HomeShelf
import com.jorhetfield.atelier.ui.home.ShelfSource
import com.jorhetfield.atelier.ui.model.ArtworkUi
import com.jorhetfield.atelier.ui.model.toErrorKind
import com.jorhetfield.atelier.ui.model.toUi
import com.jorhetfield.atelier.ui.search.LoadMore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val FIRST_PAGE = 1

// The full list behind a Home shelf. Unlike the search it has one source for its whole life, so there
// is no term to guard against: leaving the screen cancels whatever is in flight with the view model.
@HiltViewModel(assistedFactory = ShelfViewModel.Factory::class)
class ShelfViewModel
    @AssistedInject
    constructor(
        @Assisted private val shelf: HomeShelf,
        private val getHighlights: GetHighlightsUseCase,
        private val searchArtworks: SearchArtworksUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<ShelfUiState>(ShelfUiState.Loading)
        val uiState: StateFlow<ShelfUiState> = _uiState.asStateFlow()

        private var nextPage = FIRST_PAGE + 1
        private var job: Job? = null

        // The Home has just loaded the first page, so it is usually cached and this runs without
        // suspending: the list is there before the first frame instead of flashing a loader.
        init {
            loadFirstPage()
        }

        fun retry() = loadFirstPage()

        // Asks for the next page; also the retry after a failed one.
        fun loadMore() {
            val current = _uiState.value as? ShelfUiState.Content ?: return
            if (current.loadMore !is LoadMore.Available && current.loadMore !is LoadMore.Failed) return

            val page = nextPage
            _uiState.value = current.copy(loadMore = LoadMore.Loading)
            job =
                viewModelScope.launch {
                    val result =
                        try {
                            fetch(page)
                        } catch (e: ArtworkException) {
                            _uiState.value = current.copy(loadMore = LoadMore.Failed(e.toErrorKind()))
                            return@launch
                        }
                    nextPage = page + 1
                    _uiState.value =
                        current.copy(
                            // distinctBy: the grid is keyed by id, so a piece that moved pages must not appear twice.
                            items = (current.items + result.toItems()).distinctBy { it.id },
                            loadMore = if (result.hasMore) LoadMore.Available else LoadMore.Done,
                        )
                }
        }

        private fun loadFirstPage() {
            job?.cancel()
            nextPage = FIRST_PAGE + 1
            _uiState.value = ShelfUiState.Loading
            job =
                viewModelScope.launch {
                    _uiState.value =
                        try {
                            val page = fetch(FIRST_PAGE)
                            val items = page.toItems()
                            if (items.isEmpty()) {
                                ShelfUiState.Empty
                            } else {
                                ShelfUiState.Content(
                                    items = items,
                                    loadMore = if (page.hasMore) LoadMore.Available else LoadMore.Done,
                                )
                            }
                        } catch (e: ArtworkException) {
                            ShelfUiState.Failure(e.toErrorKind())
                        }
                }
        }

        private suspend fun fetch(page: Int): SearchPage =
            when (val source = shelf.source) {
                ShelfSource.Highlights -> getHighlights(page)
                is ShelfSource.Search -> searchArtworks(source.query, page)
            }

        // Same rule as the Home: a card is the picture, so a piece without one is left out.
        private fun SearchPage.toItems(): List<ArtworkUi> = artworks.filter { it.image != null }.map { it.toUi() }

        @AssistedFactory
        interface Factory {
            fun create(shelf: HomeShelf): ShelfViewModel
        }
    }

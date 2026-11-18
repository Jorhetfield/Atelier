package com.jorhetfield.atelier.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.usecase.GetHighlightsUseCase
import com.jorhetfield.atelier.domain.usecase.SearchArtworksUseCase
import com.jorhetfield.atelier.ui.model.toErrorKind
import com.jorhetfield.atelier.ui.model.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val getHighlights: GetHighlightsUseCase,
        private val searchArtworks: SearchArtworksUseCase,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow(HomeUiState(HomeShelf.entries.map { ShelfUi(it, ShelfContent.Loading) }))
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        init {
            HomeShelf.entries.forEach(::load)
        }

        fun retry() {
            _uiState.value.shelves
                .filter { it.content is ShelfContent.Failed }
                .forEach { load(it.shelf) }
        }

        // Every shelf loads on its own, so a slow or failing one never holds back the others.
        private fun load(shelf: HomeShelf) {
            update(shelf, ShelfContent.Loading)
            viewModelScope.launch {
                val content =
                    try {
                        // A carousel card is the picture; without one it would be an empty gradient.
                        ShelfContent.Loaded(fetch(shelf).filter { it.image != null }.map { it.toUi() })
                    } catch (e: ArtworkException) {
                        ShelfContent.Failed(e.toErrorKind())
                    }
                update(shelf, content)
            }
        }

        private suspend fun fetch(shelf: HomeShelf): List<Artwork> =
            when (val source = shelf.source) {
                ShelfSource.Highlights -> getHighlights().artworks
                is ShelfSource.Search -> searchArtworks(source.query).artworks
            }

        private fun update(
            shelf: HomeShelf,
            content: ShelfContent,
        ) {
            _uiState.update { state ->
                state.copy(shelves = state.shelves.map { if (it.shelf == shelf) it.copy(content = content) else it })
            }
        }
    }

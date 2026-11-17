package com.jorhetfield.atelier.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.usecase.SearchArtworksUseCase
import com.jorhetfield.atelier.ui.model.ArtworkUi
import com.jorhetfield.atelier.ui.model.toErrorKind
import com.jorhetfield.atelier.ui.model.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

internal const val SEARCH_DEBOUNCE_MS = 300L

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val searchArtworks: SearchArtworksUseCase,
    ) : ViewModel() {
        private val query = MutableStateFlow("")
        private val content = MutableStateFlow<SearchContent>(SearchContent.Idle)

        // Channels keep an event until the pipeline takes it, unlike a SharedFlow with no collector yet.
        private val submissions = Channel<Unit>(Channel.CONFLATED)
        private val retries = Channel<Unit>(Channel.CONFLATED)

        val uiState: StateFlow<SearchUiState> =
            combine(query, content, ::SearchUiState)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

        init {
            val typed =
                merge(
                    query.debounce(SEARCH_DEBOUNCE_MS),
                    submissions.receiveAsFlow().map { query.value },
                ).map { it.trim() }.distinctUntilChanged()

            // flatMapLatest cancels the previous search the moment a new term arrives, and that
            // cancellation travels down to the HTTP call, so a stale result can never be emitted.
            merge(typed, retries.receiveAsFlow().map { query.value.trim() })
                .flatMapLatest(::search)
                .onEach { content.value = it }
                .launchIn(viewModelScope)
        }

        fun onQueryChange(text: String) {
            query.value = text
        }

        fun onSubmit() {
            if (content.value is SearchContent.Failure) retry() else submissions.trySend(Unit)
        }

        fun retry() {
            retries.trySend(Unit)
        }

        private fun search(term: String): Flow<SearchContent> =
            flow {
                if (term.isEmpty()) {
                    emit(SearchContent.Idle)
                    return@flow
                }
                emit(SearchContent.Loading(previous = content.value.visibleItems()))
                emit(
                    try {
                        val artworks = searchArtworks(term)
                        if (artworks.isEmpty()) {
                            SearchContent.NoResults(term)
                        } else {
                            SearchContent.Results(artworks.map { it.toUi() })
                        }
                    } catch (e: ArtworkException) {
                        SearchContent.Failure(e.toErrorKind())
                    },
                )
            }

        private fun SearchContent.visibleItems(): List<ArtworkUi> =
            when (this) {
                is SearchContent.Results -> items
                is SearchContent.Loading -> previous
                else -> emptyList()
            }
    }

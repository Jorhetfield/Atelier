package com.jorhetfield.atelier.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.model.SearchPage
import com.jorhetfield.atelier.domain.usecase.SearchArtworksUseCase
import com.jorhetfield.atelier.ui.model.ArtworkUi
import com.jorhetfield.atelier.ui.model.toErrorKind
import com.jorhetfield.atelier.ui.model.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
import kotlinx.coroutines.launch
import javax.inject.Inject

internal const val SEARCH_DEBOUNCE_MS = 300L
private const val FIRST_PAGE = 1

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

        // Paging bookkeeping for the search on screen. The term guards a late page from landing in the
        // results of a different search; the job is cancelled as soon as a new search begins.
        private var activeTerm = ""
        private var nextPage = FIRST_PAGE + 1
        private var loadMoreJob: Job? = null

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

        // Asks for the next page of the current search; also the retry after a failed page.
        fun loadMore() {
            val current = content.value as? SearchContent.Results ?: return
            if (current.loadMore !is LoadMore.Available && current.loadMore !is LoadMore.Failed) return

            val term = activeTerm
            val page = nextPage
            content.value = current.copy(loadMore = LoadMore.Loading)
            loadMoreJob =
                viewModelScope.launch {
                    val result =
                        try {
                            searchArtworks(term, page)
                        } catch (e: ArtworkException) {
                            publishPage(term) { it.copy(loadMore = LoadMore.Failed(e.toErrorKind())) }
                            return@launch
                        }
                    if (publishPage(term) { it.append(result) }) nextPage = page + 1
                }
        }

        // The page only lands if the search it belongs to is still the one on screen.
        private fun publishPage(
            term: String,
            change: (SearchContent.Results) -> SearchContent.Results,
        ): Boolean {
            val current = content.value as? SearchContent.Results ?: return false
            if (term != activeTerm) return false
            content.value = change(current)
            return true
        }

        private fun SearchContent.Results.append(page: SearchPage) =
            copy(
                // distinctBy: the grid is keyed by id, and a result that shifted pages must not appear twice.
                items = (items + page.artworks.map { it.toUi() }).distinctBy { it.id },
                loadMore = if (page.hasMore) LoadMore.Available else LoadMore.Done,
            )

        private fun search(term: String): Flow<SearchContent> =
            flow {
                loadMoreJob?.cancel()
                activeTerm = term
                nextPage = FIRST_PAGE + 1
                if (term.isEmpty()) {
                    emit(SearchContent.Idle)
                    return@flow
                }
                emit(SearchContent.Loading(previous = content.value.visibleItems()))
                emit(
                    try {
                        val page = searchArtworks(term, FIRST_PAGE)
                        if (page.artworks.isEmpty()) {
                            SearchContent.NoResults(term)
                        } else {
                            SearchContent.Results(
                                items = page.artworks.map { it.toUi() },
                                loadMore = if (page.hasMore) LoadMore.Available else LoadMore.Done,
                            )
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

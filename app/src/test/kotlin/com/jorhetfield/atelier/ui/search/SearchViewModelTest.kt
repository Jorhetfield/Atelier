package com.jorhetfield.atelier.ui.search

import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.model.SearchPage
import com.jorhetfield.atelier.domain.usecase.SearchArtworksUseCase
import com.jorhetfield.atelier.mocks.FakeArtworkRepository
import com.jorhetfield.atelier.mocks.MainDispatcherExtension
import com.jorhetfield.atelier.mocks.artwork
import com.jorhetfield.atelier.ui.model.ErrorKind
import com.jorhetfield.atelier.ui.model.toUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val repository = FakeArtworkRepository()

    // Lazy so it is built after the extension has installed the test Main dispatcher.
    private val viewModel by lazy { SearchViewModel(SearchArtworksUseCase(repository)) }

    private val debounce get() = SEARCH_DEBOUNCE_MS

    private fun TestScope.recordStates(): List<SearchUiState> {
        val seen = mutableListOf<SearchUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect { seen += it } }
        return seen
    }

    private fun List<SearchUiState>.contents() =
        map { it.content }.fold(emptyList<SearchContent>()) { acc, content ->
            if (acc.lastOrNull() == content) acc else acc + content
        }

    private fun TestScope.type(text: String) {
        viewModel.onQueryChange(text)
        advanceTimeBy(debounce)
        runCurrent()
    }

    @Test
    fun `starts idle with an empty query`() =
        runTest {
            assertEquals(SearchUiState(), recordStates().single())
        }

    @Test
    fun `the field updates at once but the search waits for the debounce`() =
        runTest {
            repository.onSearch = { listOf(artwork(1)) }
            val seen = recordStates()

            viewModel.onQueryChange("monet")
            runCurrent()
            assertEquals("monet", seen.last().query)
            assertEquals(SearchContent.Idle, seen.last().content)

            advanceTimeBy(debounce - 1)
            runCurrent()
            assertTrue(repository.searches.isEmpty())

            advanceTimeBy(1)
            runCurrent()
            assertEquals(listOf("monet"), repository.searches)
            assertEquals(SearchContent.Results(listOf(artwork(1).toUi())), seen.last().content)
        }

    @Test
    fun `fast typing only searches for the final text`() =
        runTest {
            viewModel.onQueryChange("m")
            advanceTimeBy(100)
            viewModel.onQueryChange("mo")
            advanceTimeBy(100)
            viewModel.onQueryChange("mon")
            advanceTimeBy(debounce)
            runCurrent()

            assertEquals(listOf("mon"), repository.searches)
        }

    @Test
    fun `a blank query goes back to idle without searching`() =
        runTest {
            repository.onSearch = { listOf(artwork(1)) }
            recordStates()
            type("monet")

            type("   ")

            assertEquals(SearchContent.Idle, viewModel.uiState.value.content)
            assertEquals(listOf("monet"), repository.searches)
        }

    @Test
    fun `a slow search never overwrites a later one`() =
        runTest {
            var slowOneFinished = false
            repository.onSearch = { term ->
                if (term == "slow") {
                    delay(1_000)
                    slowOneFinished = true
                    listOf(artwork(1, title = "From the slow search"))
                } else {
                    delay(100)
                    listOf(artwork(2, title = "From the fast search"))
                }
            }
            val seen = recordStates()

            type("slow")
            type("fast")
            advanceTimeBy(5_000)
            runCurrent()

            assertEquals(
                SearchContent.Results(listOf(artwork(2, title = "From the fast search").toUi())),
                viewModel.uiState.value.content,
            )
            assertFalse(slowOneFinished)
            val titlesEverShown =
                seen
                    .flatMap {
                        (it.content as? SearchContent.Results)?.items.orEmpty()
                    }.map { it.title }
            assertFalse("From the slow search" in titlesEverShown)
        }

    @Test
    fun `the search in flight is cancelled when a new term arrives`() =
        runTest {
            var cancelled = false
            repository.onSearch = { term ->
                if (term == "slow") {
                    try {
                        delay(1_000)
                    } catch (e: CancellationException) {
                        cancelled = true
                        throw e
                    }
                }
                emptyList()
            }
            recordStates()

            type("slow")
            assertFalse(cancelled)
            type("fast")

            assertTrue(cancelled)
        }

    @Test
    fun `a search with no matches says so`() =
        runTest {
            repository.onSearch = { emptyList() }
            recordStates()

            type("qwertyuiop")

            assertEquals(SearchContent.NoResults("qwertyuiop"), viewModel.uiState.value.content)
        }

    @Test
    fun `no connection is reported as offline`() =
        failureIsReportedAs(ArtworkException.NetworkUnavailable(), ErrorKind.Offline)

    @Test
    fun `rate limiting is reported as such`() =
        failureIsReportedAs(ArtworkException.RateLimited(), ErrorKind.RateLimited)

    @Test
    fun `any other failure is generic`() = failureIsReportedAs(ArtworkException.ServerError(500), ErrorKind.Generic)

    private fun failureIsReportedAs(
        failure: ArtworkException,
        expected: ErrorKind,
    ) = runTest {
        repository.onSearch = { throw failure }
        recordStates()

        type("monet")

        assertEquals(SearchContent.Failure(expected), viewModel.uiState.value.content)
    }

    @Test
    fun `previous results stay behind the loader but vanish when the new search fails`() =
        runTest {
            val first = listOf(artwork(1, title = "First"))
            repository.onSearch = { term ->
                delay(100)
                if (term == "second") throw ArtworkException.ServerError(500) else first
            }
            val seen = recordStates()

            type("first")
            advanceTimeBy(100)
            type("second")
            advanceTimeBy(100)
            runCurrent()

            assertEquals(
                listOf(
                    SearchContent.Idle,
                    SearchContent.Loading(),
                    SearchContent.Results(first.map { it.toUi() }),
                    SearchContent.Loading(previous = first.map { it.toUi() }),
                    SearchContent.Failure(ErrorKind.Generic),
                ),
                seen.contents(),
            )
        }

    @Test
    fun `retrying runs the failed search again`() =
        runTest {
            var attempts = 0
            repository.onSearch = {
                if (attempts++ == 0) throw ArtworkException.NetworkUnavailable() else listOf(artwork(1))
            }
            recordStates()
            type("monet")
            assertEquals(SearchContent.Failure(ErrorKind.Offline), viewModel.uiState.value.content)

            viewModel.retry()
            runCurrent()

            assertEquals(listOf("monet", "monet"), repository.searches)
            assertEquals(SearchContent.Results(listOf(artwork(1).toUi())), viewModel.uiState.value.content)
        }

    @Test
    fun `submitting searches at once and the debounce does not repeat it`() =
        runTest {
            repository.onSearch = { listOf(artwork(1)) }
            recordStates()

            viewModel.onQueryChange("monet")
            viewModel.onSubmit()
            runCurrent()
            assertEquals(listOf("monet"), repository.searches)

            advanceTimeBy(debounce)
            runCurrent()
            assertEquals(listOf("monet"), repository.searches)
        }

    @Test
    fun `submitting after a failure retries it`() =
        runTest {
            var attempts = 0
            repository.onSearch = {
                if (attempts++ == 0) throw ArtworkException.RateLimited() else listOf(artwork(1))
            }
            recordStates()
            type("monet")

            viewModel.onSubmit()
            runCurrent()

            assertEquals(2, repository.searches.size)
            assertTrue(viewModel.uiState.value.content is SearchContent.Results)
        }

    // --- paging ---

    private fun page(
        vararg ids: Int,
        hasMore: Boolean,
    ) = SearchPage(ids.map { artwork(it) }, hasMore)

    private fun TestScope.loadMore() {
        viewModel.loadMore()
        runCurrent()
    }

    private val results get() = viewModel.uiState.value.content as SearchContent.Results

    @Test
    fun `a first page that has a successor offers to load more`() =
        runTest {
            repository.onSearchPage = { _, _ -> page(1, 2, hasMore = true) }
            recordStates()

            type("monet")

            assertEquals(LoadMore.Available, results.loadMore)
        }

    @Test
    fun `a last page offers nothing more`() =
        runTest {
            repository.onSearchPage = { _, _ -> page(1, 2, hasMore = false) }
            recordStates()

            type("monet")
            loadMore()

            assertEquals(LoadMore.Done, results.loadMore)
            assertEquals(listOf("monet" to 1), repository.searchRequests)
        }

    @Test
    fun `loading more adds the next page below the ones already shown`() =
        runTest {
            repository.onSearchPage = { _, page ->
                when (page) {
                    1 -> page(1, 2, hasMore = true)
                    2 -> page(3, 4, hasMore = true)
                    else -> page(5, hasMore = false)
                }
            }
            recordStates()
            type("monet")

            loadMore()
            assertEquals(listOf(1, 2, 3, 4), results.items.map { it.id })
            assertEquals(LoadMore.Available, results.loadMore)

            loadMore()
            assertEquals(listOf(1, 2, 3, 4, 5), results.items.map { it.id })
            assertEquals(LoadMore.Done, results.loadMore)
            assertEquals(listOf("monet" to 1, "monet" to 2, "monet" to 3), repository.searchRequests)
        }

    @Test
    fun `a page is not requested twice while it is on its way`() =
        runTest {
            val answer = CompletableDeferred<SearchPage>()
            repository.onSearchPage = { _, page -> if (page == 1) page(1, hasMore = true) else answer.await() }
            recordStates()
            type("monet")

            loadMore()
            assertEquals(LoadMore.Loading, results.loadMore)
            loadMore()
            loadMore()

            assertEquals(2, repository.searchRequests.size)
            answer.complete(page(2, hasMore = false))
            runCurrent()
            assertEquals(listOf(1, 2), results.items.map { it.id })
        }

    @Test
    fun `a failed page keeps what is shown and a retry asks for the same page`() =
        runTest {
            var attempts = 0
            repository.onSearchPage = { _, page ->
                when {
                    page == 1 -> page(1, hasMore = true)
                    attempts++ == 0 -> throw ArtworkException.NetworkUnavailable()
                    else -> page(2, hasMore = false)
                }
            }
            recordStates()
            type("monet")

            loadMore()
            assertEquals(listOf(1), results.items.map { it.id })
            assertEquals(LoadMore.Failed(ErrorKind.Offline), results.loadMore)

            loadMore()
            assertEquals(listOf(1, 2), results.items.map { it.id })
            assertEquals(listOf("monet" to 1, "monet" to 2, "monet" to 2), repository.searchRequests)
        }

    @Test
    fun `an artwork that shows up on two pages is listed once`() =
        runTest {
            repository.onSearchPage = { _, page ->
                if (page == 1) page(1, 2, hasMore = true) else page(2, 3, hasMore = false)
            }
            recordStates()
            type("monet")

            loadMore()

            assertEquals(listOf(1, 2, 3), results.items.map { it.id })
        }

    @Test
    fun `a slow page of an earlier search never lands in a newer one`() =
        runTest {
            val slowPage = CompletableDeferred<SearchPage>()
            repository.onSearchPage = { term, page ->
                when {
                    term == "monet" && page == 1 -> page(1, hasMore = true)
                    term == "monet" -> slowPage.await()
                    else -> page(100, hasMore = false)
                }
            }
            val seen = recordStates()
            type("monet")
            loadMore()

            type("picasso")
            slowPage.complete(page(2, hasMore = false))
            runCurrent()

            assertEquals(SearchContent.Results(listOf(artwork(100).toUi())), viewModel.uiState.value.content)
            val everShown = seen.flatMap { (it.content as? SearchContent.Results)?.items.orEmpty() }.map { it.id }
            assertFalse(2 in everShown)
        }

    @Test
    fun `the page in flight is cancelled when a new search begins`() =
        runTest {
            var cancelled = false
            repository.onSearchPage = { term, page ->
                if (term == "monet" && page == 2) {
                    try {
                        awaitCancellation()
                    } catch (e: CancellationException) {
                        cancelled = true
                        throw e
                    }
                }
                page(1, hasMore = term == "monet")
            }
            recordStates()
            type("monet")
            loadMore()
            assertFalse(cancelled)

            type("picasso")

            assertTrue(cancelled)
        }

    @Test
    fun `a new search starts again from the first page`() =
        runTest {
            repository.onSearchPage = { _, page -> page(page, hasMore = true) }
            recordStates()
            type("monet")
            loadMore()
            loadMore()

            type("picasso")
            loadMore()

            assertEquals(
                listOf("monet" to 1, "monet" to 2, "monet" to 3, "picasso" to 1, "picasso" to 2),
                repository.searchRequests,
            )
        }

    @Test
    fun `asking for more with no results on screen does nothing`() =
        runTest {
            recordStates()

            loadMore()

            assertTrue(repository.searchRequests.isEmpty())
        }
}

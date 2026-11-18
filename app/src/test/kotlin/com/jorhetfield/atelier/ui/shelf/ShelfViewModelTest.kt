package com.jorhetfield.atelier.ui.shelf

import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.model.ArtworkImage
import com.jorhetfield.atelier.domain.model.SearchPage
import com.jorhetfield.atelier.domain.usecase.GetHighlightsUseCase
import com.jorhetfield.atelier.domain.usecase.SearchArtworksUseCase
import com.jorhetfield.atelier.mocks.FakeArtworkRepository
import com.jorhetfield.atelier.mocks.MainDispatcherExtension
import com.jorhetfield.atelier.mocks.artwork
import com.jorhetfield.atelier.ui.home.HomeShelf
import com.jorhetfield.atelier.ui.model.ErrorKind
import com.jorhetfield.atelier.ui.search.LoadMore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class ShelfViewModelTest {
    // Unconfined stands in for Main.immediate: work that does not suspend runs inside the constructor.
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension(UnconfinedTestDispatcher())

    private val repository = FakeArtworkRepository()
    private val picture = ArtworkImage(id = "x", altText = null, placeholder = null, width = 1000, height = 800)

    private fun pictured(id: Int) = artwork(id, image = picture)

    private fun page(
        vararg ids: Int,
        hasMore: Boolean,
    ) = SearchPage(ids.map(::pictured), hasMore)

    private fun viewModel(shelf: HomeShelf) =
        ShelfViewModel(shelf, GetHighlightsUseCase(repository), SearchArtworksUseCase(repository))

    private fun ShelfViewModel.items() = (uiState.value as ShelfUiState.Content).items.map { it.id }

    private fun ShelfViewModel.loadMoreState() = (uiState.value as ShelfUiState.Content).loadMore

    @Test
    fun `a search shelf shows the first page of its own search`() {
        repository.onSearchPage = { _, _ -> page(1, 2, hasMore = true) }

        val viewModel = viewModel(HomeShelf.Monet)

        assertEquals(listOf(1, 2), viewModel.items())
        assertEquals(LoadMore.Available, viewModel.loadMoreState())
        assertEquals(listOf("monet" to 1), repository.searchRequests)
        assertTrue(repository.highlightsPages.isEmpty())
    }

    @Test
    fun `the highlights shelf reads the highlights, not a search`() {
        repository.onGetHighlightsPage = { page(7, 8, hasMore = false) }

        val viewModel = viewModel(HomeShelf.Highlights)

        assertEquals(listOf(7, 8), viewModel.items())
        assertEquals(LoadMore.Done, viewModel.loadMoreState())
        assertEquals(listOf(1), repository.highlightsPages)
        assertTrue(repository.searchRequests.isEmpty())
    }

    @Test
    fun `a first page the repository has at hand is shown from the very first state`() {
        repository.onSearchPage = { _, _ -> page(1, hasMore = false) }

        // No Loading in between: the Home has just fetched it, so it comes straight from the cache.
        assertTrue(viewModel(HomeShelf.Picasso).uiState.value is ShelfUiState.Content)
    }

    @Test
    fun `loading shows while the first page is really on its way`() {
        val answer = CompletableDeferred<SearchPage>()
        repository.onSearchPage = { _, _ -> answer.await() }

        val viewModel = viewModel(HomeShelf.Monet)
        assertEquals(ShelfUiState.Loading, viewModel.uiState.value)

        answer.complete(page(1, hasMore = false))
        assertEquals(listOf(1), viewModel.items())
    }

    @Test
    fun `pieces without a picture are left out, as on the Home`() {
        repository.onSearchPage = { _, _ ->
            SearchPage(listOf(pictured(1), artwork(2, image = null), pictured(3)), hasMore = false)
        }

        assertEquals(listOf(1, 3), viewModel(HomeShelf.Monet).items())
    }

    @Test
    fun `a first page with nothing to show is reported as empty`() {
        repository.onSearchPage = { _, _ -> SearchPage(listOf(artwork(1, image = null)), hasMore = false) }

        assertEquals(ShelfUiState.Empty, viewModel(HomeShelf.Monet).uiState.value)
    }

    @Test
    fun `loading more appends the next page below`() {
        repository.onSearchPage = { _, page ->
            if (page == 1) page(1, 2, hasMore = true) else page(3, 4, hasMore = true)
        }
        val viewModel = viewModel(HomeShelf.VanGogh)

        viewModel.loadMore()

        assertEquals(listOf(1, 2, 3, 4), viewModel.items())
        assertEquals(LoadMore.Available, viewModel.loadMoreState())
        assertEquals(listOf("van gogh" to 1, "van gogh" to 2), repository.searchRequests)
    }

    @Test
    fun `the last page ends the list`() {
        repository.onSearchPage = { _, page ->
            if (page == 1) page(1, hasMore = true) else page(2, hasMore = false)
        }
        val viewModel = viewModel(HomeShelf.Monet)

        viewModel.loadMore()
        viewModel.loadMore()

        assertEquals(LoadMore.Done, viewModel.loadMoreState())
        assertEquals(2, repository.searchRequests.size)
    }

    @Test
    fun `highlights are paged through the highlights call`() {
        repository.onGetHighlightsPage = { page ->
            if (page == 1) page(1, 2, hasMore = true) else page(3, hasMore = false)
        }
        val viewModel = viewModel(HomeShelf.Highlights)

        viewModel.loadMore()

        assertEquals(listOf(1, 2, 3), viewModel.items())
        assertEquals(listOf(1, 2), repository.highlightsPages)
    }

    @Test
    fun `a page is not requested twice while it is on its way`() {
        val answer = CompletableDeferred<SearchPage>()
        repository.onSearchPage = { _, page -> if (page == 1) page(1, hasMore = true) else answer.await() }
        val viewModel = viewModel(HomeShelf.Monet)

        viewModel.loadMore()
        viewModel.loadMore()
        viewModel.loadMore()

        assertEquals(LoadMore.Loading, viewModel.loadMoreState())
        assertEquals(2, repository.searchRequests.size)
    }

    @Test
    fun `a failed page keeps the list and a retry asks for the same page`() {
        var attempts = 0
        repository.onSearchPage = { _, page ->
            when {
                page == 1 -> page(1, hasMore = true)
                attempts++ == 0 -> throw ArtworkException.NetworkUnavailable()
                else -> page(2, hasMore = false)
            }
        }
        val viewModel = viewModel(HomeShelf.Monet)

        viewModel.loadMore()
        assertEquals(listOf(1), viewModel.items())
        assertEquals(LoadMore.Failed(ErrorKind.Offline), viewModel.loadMoreState())

        viewModel.loadMore()
        assertEquals(listOf(1, 2), viewModel.items())
        assertEquals(listOf("monet" to 1, "monet" to 2, "monet" to 2), repository.searchRequests)
    }

    @Test
    fun `an artwork that shows up on two pages is listed once`() {
        repository.onSearchPage = { _, page ->
            if (page == 1) page(1, 2, hasMore = true) else page(2, 3, hasMore = false)
        }
        val viewModel = viewModel(HomeShelf.Monet)

        viewModel.loadMore()

        assertEquals(listOf(1, 2, 3), viewModel.items())
    }

    @Test
    fun `a failed first page is reported and retrying loads it`() {
        var attempts = 0
        repository.onSearchPage = { _, _ ->
            if (attempts++ == 0) throw ArtworkException.RateLimited() else page(1, hasMore = false)
        }
        val viewModel = viewModel(HomeShelf.Monet)
        assertEquals(ShelfUiState.Failure(ErrorKind.RateLimited), viewModel.uiState.value)

        viewModel.retry()

        assertEquals(listOf(1), viewModel.items())
    }

    @Test
    fun `asking for more while the list is not showing does nothing`() {
        repository.onSearchPage = { _, _ -> throw ArtworkException.NetworkUnavailable() }
        val viewModel = viewModel(HomeShelf.Monet)

        viewModel.loadMore()

        assertEquals(1, repository.searchRequests.size)
    }
}

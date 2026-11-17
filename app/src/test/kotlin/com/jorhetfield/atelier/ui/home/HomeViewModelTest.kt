package com.jorhetfield.atelier.ui.home

import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.model.ArtworkImage
import com.jorhetfield.atelier.domain.usecase.GetHighlightsUseCase
import com.jorhetfield.atelier.domain.usecase.SearchArtworksUseCase
import com.jorhetfield.atelier.mocks.FakeArtworkRepository
import com.jorhetfield.atelier.mocks.MainDispatcherExtension
import com.jorhetfield.atelier.mocks.artwork
import com.jorhetfield.atelier.ui.model.ErrorKind
import com.jorhetfield.atelier.ui.model.toUi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class HomeViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val repository = FakeArtworkRepository()

    // The home only shows artworks that have a picture, so the ones in these tests carry one.
    private val picture = ArtworkImage(id = "x", altText = null, placeholder = null, width = 1000, height = 800)

    private fun pictured(
        id: Int,
        title: String = "Artwork $id",
    ) = artwork(id, title, image = picture)

    private fun viewModel() = HomeViewModel(GetHighlightsUseCase(repository), SearchArtworksUseCase(repository))

    private fun HomeViewModel.content(shelf: HomeShelf) =
        uiState.value.shelves
            .first { it.shelf == shelf }
            .content

    private fun TestScope.loaded(): HomeViewModel = viewModel().also { advanceUntilIdle() }

    @Test
    fun `every shelf starts loading`() =
        runTest {
            val viewModel = viewModel()

            assertTrue(
                viewModel.uiState.value.shelves
                    .all { it.content == ShelfContent.Loading },
            )
            assertEquals(
                HomeShelf.entries,
                viewModel.uiState.value.shelves
                    .map { it.shelf },
            )
        }

    @Test
    fun `the highlights come from the museum's picks and the others from a search`() =
        runTest {
            repository.onGetHighlights = { listOf(pictured(1, title = "Highlight")) }
            repository.onSearch = { query -> listOf(pictured(2, title = "Found for $query")) }

            val viewModel = loaded()

            assertEquals(
                ShelfContent.Loaded(listOf(pictured(1, title = "Highlight").toUi())),
                viewModel.content(HomeShelf.Highlights),
            )
            assertEquals(
                ShelfContent.Loaded(listOf(pictured(2, title = "Found for picasso").toUi())),
                viewModel.content(HomeShelf.Picasso),
            )
            assertEquals(listOf("monet", "van gogh", "picasso", "japanese prints"), repository.searches)
            assertEquals(1, repository.highlightsCalls)
        }

    @Test
    fun `artworks without a picture are left out of the shelves, keeping the order`() =
        runTest {
            repository.onGetHighlights = {
                listOf(pictured(1), artwork(2, image = null), pictured(3))
            }

            val viewModel = loaded()

            val items = (viewModel.content(HomeShelf.Highlights) as ShelfContent.Loaded).items
            assertEquals(listOf(1, 3), items.map { it.id })
        }

    @Test
    fun `a shelf where nothing has a picture is hidden`() =
        runTest {
            repository.onGetHighlights = { listOf(artwork(1, image = null), artwork(2, image = null)) }

            val viewModel = loaded()

            assertTrue(
                HomeShelf.Highlights !in
                    viewModel.uiState.value.visibleShelves
                        .map { it.shelf },
            )
        }

    @Test
    fun `a failing shelf does not take the others down`() =
        runTest {
            repository.onGetHighlights = { listOf(pictured(1)) }
            repository.onSearch = { query ->
                if (query == "picasso") throw ArtworkException.RateLimited() else listOf(pictured(2))
            }

            val viewModel = loaded()

            assertEquals(ShelfContent.Failed(ErrorKind.RateLimited), viewModel.content(HomeShelf.Picasso))
            assertTrue(viewModel.content(HomeShelf.Monet) is ShelfContent.Loaded)
            assertTrue(
                HomeShelf.Picasso !in
                    viewModel.uiState.value.visibleShelves
                        .map { it.shelf },
            )
            assertNull(viewModel.uiState.value.failure)
        }

    @Test
    fun `when every shelf fails the whole screen reports it`() =
        runTest {
            repository.onGetHighlights = { throw ArtworkException.NetworkUnavailable() }
            repository.onSearch = { throw ArtworkException.NetworkUnavailable() }

            val viewModel = loaded()

            assertEquals(ErrorKind.Offline, viewModel.uiState.value.failure)
            assertTrue(
                viewModel.uiState.value.visibleShelves
                    .isEmpty(),
            )
        }

    @Test
    fun `a shelf that comes back empty is hidden`() =
        runTest {
            repository.onGetHighlights = { listOf(pictured(1)) }
            repository.onSearch = { emptyList() }

            val viewModel = loaded()

            assertEquals(
                listOf(HomeShelf.Highlights),
                viewModel.uiState.value.visibleShelves
                    .map { it.shelf },
            )
            assertNull(viewModel.uiState.value.failure)
        }

    @Test
    fun `retrying reloads only the shelves that failed`() =
        runTest {
            var attempts = 0
            repository.onGetHighlights = { listOf(pictured(1)) }
            repository.onSearch = { query ->
                if (query == "monet" &&
                    attempts++ == 0
                ) {
                    throw ArtworkException.NetworkUnavailable()
                } else {
                    listOf(pictured(2))
                }
            }
            val viewModel = loaded()
            assertTrue(viewModel.content(HomeShelf.Monet) is ShelfContent.Failed)

            viewModel.retry()
            advanceUntilIdle()

            assertTrue(viewModel.content(HomeShelf.Monet) is ShelfContent.Loaded)
            assertEquals(1, repository.highlightsCalls)
            assertEquals(listOf("monet", "van gogh", "picasso", "japanese prints", "monet"), repository.searches)
        }
}

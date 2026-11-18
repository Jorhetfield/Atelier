package com.jorhetfield.atelier.ui.detail

import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.usecase.GetArtworkUseCase
import com.jorhetfield.atelier.mocks.FakeArtworkRepository
import com.jorhetfield.atelier.mocks.MainDispatcherExtension
import com.jorhetfield.atelier.mocks.artwork
import com.jorhetfield.atelier.ui.model.ErrorKind
import com.jorhetfield.atelier.ui.model.toDetailUi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    // Unconfined stands in for Main.immediate: work that does not suspend runs inside the constructor.
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension(UnconfinedTestDispatcher())

    private val repository = FakeArtworkRepository()

    private fun viewModel(id: Int = 27992) = DetailViewModel(id, GetArtworkUseCase(repository))

    @Test
    fun `an artwork the repository has at hand is shown from the very first state`() {
        repository.onGetArtwork = { id -> artwork(id, title = "La Grande Jatte") }

        val viewModel = viewModel(27992)

        assertEquals(
            DetailUiState.Success(artwork(27992, title = "La Grande Jatte").toDetailUi()),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `loading is shown only while the repository is really waiting`() {
        val answer = CompletableDeferred<Artwork>()
        repository.onGetArtwork = { answer.await() }

        val viewModel = viewModel(27992)
        assertEquals(DetailUiState.Loading, viewModel.uiState.value)

        answer.complete(artwork(27992))
        assertEquals(DetailUiState.Success(artwork(27992).toDetailUi()), viewModel.uiState.value)
    }

    @Test
    fun `a missing artwork is reported as not found`() {
        repository.onGetArtwork = { throw ArtworkException.NotFound() }

        assertEquals(DetailUiState.Failure(ErrorKind.NotFound), viewModel().uiState.value)
    }

    @Test
    fun `retrying after a failure loads the artwork`() {
        var attempts = 0
        repository.onGetArtwork = { id ->
            if (attempts++ == 0) throw ArtworkException.NetworkUnavailable() else artwork(id)
        }
        val viewModel = viewModel()
        assertEquals(DetailUiState.Failure(ErrorKind.Offline), viewModel.uiState.value)

        viewModel.retry()

        assertEquals(DetailUiState.Success(artwork(27992).toDetailUi()), viewModel.uiState.value)
    }

    @Test
    fun `a retry shows loading again while it waits`() {
        val answer = CompletableDeferred<Artwork>()
        var attempts = 0
        repository.onGetArtwork =
            { if (attempts++ == 0) throw ArtworkException.NetworkUnavailable() else answer.await() }
        val viewModel = viewModel()

        viewModel.retry()

        assertEquals(DetailUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `the artwork is requested once, however often the screen is shown`() {
        var calls = 0
        repository.onGetArtwork = { id ->
            calls++
            artwork(id)
        }
        val viewModel = viewModel()

        // The screen goes off while the viewer is on top and comes back: it reads the same state.
        repeat(3) { viewModel.uiState.value }

        assertEquals(1, calls)
    }
}

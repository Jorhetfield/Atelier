package com.jorhetfield.atelier.ui.detail

import app.cash.turbine.test
import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.usecase.GetArtworkUseCase
import com.jorhetfield.atelier.mocks.FakeArtworkRepository
import com.jorhetfield.atelier.mocks.MainDispatcherExtension
import com.jorhetfield.atelier.mocks.artwork
import com.jorhetfield.atelier.ui.model.ErrorKind
import com.jorhetfield.atelier.ui.model.toDetailUi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class DetailViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val repository = FakeArtworkRepository()

    private fun viewModel(id: Int = 27992) = DetailViewModel(id, GetArtworkUseCase(repository))

    @Test
    fun `loads the artwork it was opened with`() =
        runTest {
            repository.onGetArtwork = { id -> artwork(id, title = "La Grande Jatte") }

            viewModel(27992).uiState.test {
                assertEquals(DetailUiState.Loading, awaitItem())
                assertEquals(
                    DetailUiState.Success(artwork(27992, title = "La Grande Jatte").toDetailUi()),
                    awaitItem(),
                )
            }
        }

    @Test
    fun `a missing artwork is reported as not found`() =
        runTest {
            repository.onGetArtwork = { throw ArtworkException.NotFound() }

            viewModel().uiState.test {
                assertEquals(DetailUiState.Loading, awaitItem())
                assertEquals(DetailUiState.Failure(ErrorKind.NotFound), awaitItem())
            }
        }

    @Test
    fun `retrying after a failure loads the artwork`() =
        runTest {
            var attempts = 0
            repository.onGetArtwork = { id ->
                if (attempts++ == 0) throw ArtworkException.NetworkUnavailable() else artwork(id)
            }
            val viewModel = viewModel()

            viewModel.uiState.test {
                assertEquals(DetailUiState.Loading, awaitItem())
                assertEquals(DetailUiState.Failure(ErrorKind.Offline), awaitItem())

                viewModel.retry()

                assertEquals(DetailUiState.Loading, awaitItem())
                assertEquals(DetailUiState.Success(artwork(27992).toDetailUi()), awaitItem())
            }
        }
}

package com.jorhetfield.atelier.domain.usecase

import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.mocks.artwork
import com.jorhetfield.atelier.domain.repository.ArtworkRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.coroutines.cancellation.CancellationException

class GetHighlightsUseCaseTest {
    private val repository = mockk<ArtworkRepository>()
    private val useCase = GetHighlightsUseCase(repository)

    @Test
    fun `returns the highlights in the order the repository gives them`() =
        runTest {
            val highlights = listOf(artwork(id = 3), artwork(id = 1), artwork(id = 2))
            coEvery { repository.getHighlights() } returns highlights

            assertEquals(highlights, useCase())
        }

    @Test
    fun `repository failures reach the caller unchanged`() =
        runTest {
            coEvery { repository.getHighlights() } throws ArtworkException.NetworkUnavailable()

            assertThrows<ArtworkException.NetworkUnavailable> { useCase() }
        }

    @Test
    fun `cancellation is not swallowed`() =
        runTest {
            coEvery { repository.getHighlights() } throws CancellationException("cancelled")

            assertThrows<CancellationException> { useCase() }
        }
}

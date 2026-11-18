package com.jorhetfield.atelier.domain.usecase

import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.mocks.artwork
import com.jorhetfield.atelier.domain.model.SearchPage
import com.jorhetfield.atelier.domain.repository.ArtworkRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
            val highlights = SearchPage(listOf(artwork(id = 3), artwork(id = 1), artwork(id = 2)), hasMore = true)
            coEvery { repository.getHighlights(1) } returns highlights

            assertEquals(highlights, useCase())
        }

    @Test
    fun `asks for the page it is given`() =
        runTest {
            val third = SearchPage(listOf(artwork(id = 25)), hasMore = false)
            coEvery { repository.getHighlights(3) } returns third

            assertEquals(third, useCase(page = 3))
            coVerify(exactly = 0) { repository.getHighlights(1) }
        }

    @Test
    fun `a page before the first is rejected`() =
        runTest {
            assertThrows<IllegalArgumentException> { useCase(page = 0) }

            coVerify(exactly = 0) { repository.getHighlights(any()) }
        }

    @Test
    fun `repository failures reach the caller unchanged`() =
        runTest {
            coEvery { repository.getHighlights(any()) } throws ArtworkException.NetworkUnavailable()

            assertThrows<ArtworkException.NetworkUnavailable> { useCase() }
        }

    @Test
    fun `cancellation is not swallowed`() =
        runTest {
            coEvery { repository.getHighlights(any()) } throws CancellationException("cancelled")

            assertThrows<CancellationException> { useCase() }
        }
}

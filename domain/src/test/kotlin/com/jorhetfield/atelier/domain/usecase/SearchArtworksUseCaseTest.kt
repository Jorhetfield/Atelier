package com.jorhetfield.atelier.domain.usecase

import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.mocks.artwork
import com.jorhetfield.atelier.domain.repository.ArtworkRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.coroutines.cancellation.CancellationException

class SearchArtworksUseCaseTest {
    private val repository = mockk<ArtworkRepository>()
    private val useCase = SearchArtworksUseCase(repository)

    @Test
    fun `returns what the repository finds`() =
        runTest {
            val found = listOf(artwork(id = 1), artwork(id = 2))
            coEvery { repository.search("monet") } returns found

            assertEquals(found, useCase("monet"))
        }

    @Test
    fun `trims the query before asking the repository`() =
        runTest {
            coEvery { repository.search("monet") } returns emptyList()

            useCase("  monet \n")

            coVerify(exactly = 1) { repository.search("monet") }
        }

    @Test
    fun `blank query returns nothing without touching the repository`() =
        runTest {
            assertEquals(emptyList<Any>(), useCase("   "))
            assertEquals(emptyList<Any>(), useCase(""))

            coVerify(exactly = 0) { repository.search(any()) }
        }

    @Test
    fun `repository failures reach the caller unchanged`() =
        runTest {
            val failure = ArtworkException.RateLimited()
            coEvery { repository.search(any()) } throws failure

            val thrown = assertThrows<ArtworkException.RateLimited> { useCase("monet") }

            assertSame(failure, thrown)
        }

    @Test
    fun `cancellation is not swallowed`() =
        runTest {
            coEvery { repository.search(any()) } throws CancellationException("cancelled")

            assertThrows<CancellationException> { useCase("monet") }
        }
}

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

class GetArtworkUseCaseTest {
    private val repository = mockk<ArtworkRepository>()
    private val useCase = GetArtworkUseCase(repository)

    @Test
    fun `returns the artwork for the given id`() =
        runTest {
            val expected = artwork(id = 27992)
            coEvery { repository.getArtwork(27992) } returns expected

            assertEquals(expected, useCase(27992))
        }

    @Test
    fun `a missing artwork surfaces as not found`() =
        runTest {
            coEvery { repository.getArtwork(any()) } throws ArtworkException.NotFound()

            assertThrows<ArtworkException.NotFound> { useCase(404) }
        }
}

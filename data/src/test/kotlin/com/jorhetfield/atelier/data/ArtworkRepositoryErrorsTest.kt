package com.jorhetfield.atelier.data

import com.jorhetfield.atelier.data.mocks.RepositoryFixture
import com.jorhetfield.atelier.domain.error.ArtworkException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ArtworkRepositoryErrorsTest {
    private val fixture = RepositoryFixture()
    private val repository = fixture.repository

    @AfterEach
    fun tearDown() = fixture.close()

    @Test
    fun `a missing artwork is reported as not found`() =
        runTest {
            fixture.enqueue("""{"status":404,"error":"Not found"}""", code = 404)

            assertThrows<ArtworkException.NotFound> { repository.getArtwork(999) }
        }

    @Test
    fun `too many requests is reported as rate limited`() =
        runTest {
            fixture.enqueue("{}", code = 429)

            assertThrows<ArtworkException.RateLimited> { repository.search("monet", 1) }
        }

    @Test
    fun `a server failure keeps its status code and the detail the api gave`() =
        runTest {
            fixture.enqueue("""{"status":403,"error":"Invalid limit","detail":"Set a smaller limit."}""", code = 403)

            val error = assertThrows<ArtworkException.ServerError> { repository.search("monet", 1) }

            assertEquals(403, error.code)
            assertEquals("Set a smaller limit.", error.message)
        }

    @Test
    fun `a response that cannot be read is a server error`() =
        runTest {
            fixture.enqueue("this is not json")

            assertThrows<ArtworkException.ServerError> { repository.search("monet", 1) }
        }

    @Test
    fun `an unreachable server is reported as network unavailable`() =
        runTest {
            fixture.server.close()

            assertThrows<ArtworkException.NetworkUnavailable> { repository.search("monet", 1) }
        }
}

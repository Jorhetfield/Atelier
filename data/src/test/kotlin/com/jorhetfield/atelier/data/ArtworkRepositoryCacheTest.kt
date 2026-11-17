package com.jorhetfield.atelier.data

import com.jorhetfield.atelier.data.mocks.RepositoryFixture
import com.jorhetfield.atelier.data.mocks.artworkJson
import com.jorhetfield.atelier.data.mocks.detailJson
import com.jorhetfield.atelier.data.mocks.searchJson
import com.jorhetfield.atelier.domain.error.ArtworkException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ArtworkRepositoryCacheTest {
    private val fixture = RepositoryFixture()
    private val repository = fixture.repository

    @AfterEach
    fun tearDown() = fixture.close()

    @Test
    fun `a repeated search is served from the cache without a network call`() =
        runTest {
            fixture.enqueue(searchJson(artworkJson(1)))

            val first = repository.search("monet")
            val second = repository.search("monet")

            assertEquals(first, second)
            assertEquals(1, fixture.server.requestCount)
        }

    @Test
    fun `an entry still valid just before the ttl is not refetched`() =
        runTest {
            fixture.enqueue(searchJson(artworkJson(1)))
            repository.search("monet")

            fixture.time += 2.minutes + 59.seconds
            repository.search("monet")

            assertEquals(1, fixture.server.requestCount)
        }

    @Test
    fun `an expired entry is fetched again and replaced`() =
        runTest {
            fixture.enqueue(searchJson(artworkJson(1, title = "Old")))
            fixture.enqueue(searchJson(artworkJson(1, title = "New")))
            assertEquals("Old", repository.search("monet").single().title)

            fixture.time += 3.minutes

            assertEquals("New", repository.search("monet").single().title)
            assertEquals(2, fixture.server.requestCount)
            assertEquals("New", repository.search("monet").single().title)
            assertEquals(2, fixture.server.requestCount)
        }

    @Test
    fun `different queries are cached separately`() =
        runTest {
            fixture.enqueue(searchJson(artworkJson(1)))
            fixture.enqueue(searchJson(artworkJson(2)))

            assertEquals(1, repository.search("monet").single().id)
            assertEquals(2, repository.search("seurat").single().id)
            assertEquals(1, repository.search("monet").single().id)
            assertEquals(2, fixture.server.requestCount)
        }

    @Test
    fun `queries differing only in case and spaces share one entry`() =
        runTest {
            fixture.enqueue(searchJson(artworkJson(1)))

            repository.search("  Monet ")
            repository.search("monet")

            assertEquals(1, fixture.server.requestCount)
            assertTrue(
                fixture.server
                    .takeRequest()
                    .url
                    .queryParameter("q") == "monet",
            )
        }

    @Test
    fun `a failed request is not cached`() =
        runTest {
            fixture.enqueue("{}", code = 500)
            fixture.enqueue(searchJson(artworkJson(1)))

            assertThrows<ArtworkException.ServerError> { repository.search("monet") }
            assertEquals(1, repository.search("monet").size)
            assertEquals(2, fixture.server.requestCount)
        }

    @Test
    fun `a search with no matches is cached too`() =
        runTest {
            fixture.enqueue(searchJson(artworkJson(1, score = 0.00002)))

            assertTrue(repository.search("qwertyuiop").isEmpty())
            assertTrue(repository.search("qwertyuiop").isEmpty())
            assertEquals(1, fixture.server.requestCount)
        }

    @Test
    fun `an artwork found by a search is available without another request`() =
        runTest {
            fixture.enqueue(searchJson(artworkJson(27992, title = "La Grande Jatte")))
            repository.search("seurat")

            val artwork = repository.getArtwork(27992)

            assertEquals("La Grande Jatte", artwork.title)
            assertEquals(1, fixture.server.requestCount)
        }

    @Test
    fun `an unknown artwork is fetched once and then cached`() =
        runTest {
            fixture.enqueue(detailJson(artworkJson(7, title = "Fetched")))

            assertEquals("Fetched", repository.getArtwork(7).title)
            assertEquals("Fetched", repository.getArtwork(7).title)

            assertEquals(1, fixture.server.requestCount)
            assertEquals(
                "/api/v1/artworks/7",
                fixture.server
                    .takeRequest()
                    .url.encodedPath,
            )
        }

    @Test
    fun `an expired artwork is fetched again`() =
        runTest {
            fixture.enqueue(detailJson(artworkJson(7, title = "Old")))
            fixture.enqueue(detailJson(artworkJson(7, title = "New")))
            repository.getArtwork(7)

            fixture.time += 3.minutes

            assertEquals("New", repository.getArtwork(7).title)
            assertEquals(2, fixture.server.requestCount)
        }
}

package com.jorhetfield.atelier.data

import com.jorhetfield.atelier.data.mocks.RepositoryFixture
import com.jorhetfield.atelier.data.mocks.artworkJson
import com.jorhetfield.atelier.data.mocks.searchJson
import com.jorhetfield.atelier.domain.error.ArtworkException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration.Companion.minutes

class ArtworkRepositoryHighlightsTest {
    private val fixture = RepositoryFixture()
    private val repository = fixture.repository

    @AfterEach
    fun tearDown() = fixture.close()

    @Test
    fun `asks for the museum's flagged artworks, best ranked first`() =
        runTest {
            fixture.enqueue(searchJson(artworkJson(1, score = null)))

            repository.getHighlights()

            val url = fixture.server.takeRequest().url
            assertEquals("true", url.queryParameter("query[term][is_boosted]"))
            assertEquals("asc", url.queryParameter("sort[boost_rank]"))
            assertEquals("12", url.queryParameter("limit"))
        }

    @Test
    fun `keeps the order the museum ranked them in`() =
        runTest {
            fixture.enqueue(
                searchJson(artworkJson(30, score = null), artworkJson(10, score = null), artworkJson(20, score = null)),
            )

            assertEquals(listOf(30, 10, 20), repository.getHighlights().map { it.id })
        }

    @Test
    fun `is not subject to the relevance filter`() =
        runTest {
            fixture.enqueue(
                searchJson(artworkJson(1, score = 0.0), artworkJson(2, score = 0.00002), artworkJson(3, score = null)),
            )

            assertEquals(3, repository.getHighlights().size)
        }

    @Test
    fun `a second call is served from the cache and an expired one is fetched again`() =
        runTest {
            fixture.enqueue(searchJson(artworkJson(1, title = "Old", score = null)))
            fixture.enqueue(searchJson(artworkJson(1, title = "New", score = null)))

            assertEquals("Old", repository.getHighlights().single().title)
            assertEquals("Old", repository.getHighlights().single().title)
            assertEquals(1, fixture.server.requestCount)

            fixture.time += 3.minutes

            assertEquals("New", repository.getHighlights().single().title)
            assertEquals(2, fixture.server.requestCount)
        }

    @Test
    fun `a highlight can be opened without another request`() =
        runTest {
            fixture.enqueue(searchJson(artworkJson(14, title = "La Grande Jatte", score = null)))
            repository.getHighlights()

            assertEquals("La Grande Jatte", repository.getArtwork(14).title)
            assertEquals(1, fixture.server.requestCount)
        }

    @Test
    fun `a failure is reported and not cached`() =
        runTest {
            fixture.enqueue("{}", code = 500)
            fixture.enqueue(searchJson(artworkJson(1, score = null)))

            assertThrows<ArtworkException.ServerError> { repository.getHighlights() }
            assertEquals(1, repository.getHighlights().size)
            assertEquals(2, fixture.server.requestCount)
        }
}

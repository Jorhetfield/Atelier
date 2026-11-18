package com.jorhetfield.atelier.data

import com.jorhetfield.atelier.data.mocks.RepositoryFixture
import com.jorhetfield.atelier.data.mocks.artworkJson
import com.jorhetfield.atelier.data.mocks.detailJson
import com.jorhetfield.atelier.data.mocks.searchJson
import com.jorhetfield.atelier.data.remote.MAX_PAGE
import com.jorhetfield.atelier.data.remote.PAGE_SIZE
import com.jorhetfield.atelier.domain.error.ArtworkException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration.Companion.minutes

class ArtworkRepositoryPagingTest {
    private val fixture = RepositoryFixture()
    private val repository = fixture.repository

    @AfterEach
    fun tearDown() = fixture.close()

    // A full page of results, all of them relevant, with consecutive ids starting at `first`.
    private fun fullPage(first: Int = 1) = searchJson(*Array(PAGE_SIZE) { artworkJson(first + it) })

    @Test
    fun `asks the api for the page it was given`() =
        runTest {
            fixture.enqueue(fullPage())
            fixture.enqueue(fullPage(first = 100))

            repository.search("monet", 1)
            repository.search("monet", 2)

            assertEquals(
                "1",
                fixture.server
                    .takeRequest()
                    .url
                    .queryParameter("page"),
            )
            assertEquals(
                "2",
                fixture.server
                    .takeRequest()
                    .url
                    .queryParameter("page"),
            )
        }

    @Test
    fun `every page of a query is cached on its own`() =
        runTest {
            fixture.enqueue(fullPage(first = 1))
            fixture.enqueue(fullPage(first = 100))

            val one = repository.search("monet", 1)
            val two = repository.search("monet", 2)
            assertEquals(2, fixture.server.requestCount)

            assertEquals(one, repository.search("monet", 1))
            assertEquals(two, repository.search("monet", 2))
            assertEquals(2, fixture.server.requestCount)
        }

    @Test
    fun `a cached first page does not stand in for the second`() =
        runTest {
            fixture.enqueue(fullPage(first = 1))
            fixture.enqueue(fullPage(first = 100))
            repository.search("monet", 1)

            val two = repository.search("monet", 2)

            assertEquals(100, two.artworks.first().id)
            assertEquals(2, fixture.server.requestCount)
        }

    @Test
    fun `an expired page is fetched again while a fresh one is not`() =
        runTest {
            fixture.enqueue(fullPage(first = 1))
            fixture.enqueue(fullPage(first = 100))
            repository.search("monet", 1)
            fixture.time += 2.minutes
            repository.search("monet", 2)

            // Page 1 is now 2 minutes older than page 2: past the ttl, page 2 still valid.
            fixture.time += 1.minutes
            fixture.enqueue(fullPage(first = 200))
            assertEquals(
                200,
                repository
                    .search("monet", 1)
                    .artworks
                    .first()
                    .id,
            )
            assertEquals(
                100,
                repository
                    .search("monet", 2)
                    .artworks
                    .first()
                    .id,
            )

            assertEquals(3, fixture.server.requestCount)
        }

    @Test
    fun `a failed page is not cached and leaves the earlier ones alone`() =
        runTest {
            fixture.enqueue(fullPage(first = 1))
            fixture.enqueue("{}", code = 500)
            fixture.enqueue(fullPage(first = 100))
            repository.search("monet", 1)

            assertThrows<ArtworkException.ServerError> { repository.search("monet", 2) }
            assertEquals(
                100,
                repository
                    .search("monet", 2)
                    .artworks
                    .first()
                    .id,
            )

            assertEquals(
                1,
                repository
                    .search("monet", 1)
                    .artworks
                    .first()
                    .id,
            )
            assertEquals(3, fixture.server.requestCount)
        }

    @Test
    fun `an artwork from a later page is available without another request`() =
        runTest {
            fixture.enqueue(fullPage(first = 1))
            fixture.enqueue(searchJson(artworkJson(500, title = "Deep cut")))
            repository.search("monet", 1)
            repository.search("monet", 2)

            val found = repository.getArtwork(500)

            assertEquals("Deep cut", found.title)
            assertEquals(2, fixture.server.requestCount)
        }

    @Test
    fun `a full page of relevant results may be followed by more`() =
        runTest {
            fixture.enqueue(fullPage())

            assertTrue(repository.search("monet", 1).hasMore)
        }

    @Test
    fun `a short page is the last one`() =
        runTest {
            fixture.enqueue(searchJson(artworkJson(1), artworkJson(2)))

            assertFalse(repository.search("monet", 1).hasMore)
        }

    @Test
    fun `a page where the relevant results run out is the last one`() =
        runTest {
            // Full page, but the tail is noise: the real matches ended here.
            val noisy = Array(PAGE_SIZE) { artworkJson(it + 1, score = if (it < 5) 50.0 else 0.00002) }
            fixture.enqueue(searchJson(*noisy))

            val page = repository.search("monet", 1)

            assertEquals(5, page.artworks.size)
            assertFalse(page.hasMore)
        }

    @Test
    fun `the last page the api allows says there is nothing more`() =
        runTest {
            fixture.enqueue(fullPage())
            fixture.enqueue(fullPage())

            assertTrue(repository.search("monet", MAX_PAGE - 1).hasMore)
            assertFalse(repository.search("monet", MAX_PAGE).hasMore)
        }

    @Test
    fun `detail lookups are unaffected by paging`() =
        runTest {
            fixture.enqueue(detailJson(artworkJson(7, title = "Direct")))

            assertEquals("Direct", repository.getArtwork(7).title)
        }
}

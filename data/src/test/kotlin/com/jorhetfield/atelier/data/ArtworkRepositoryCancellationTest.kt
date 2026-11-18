package com.jorhetfield.atelier.data

import com.jorhetfield.atelier.data.mocks.RepositoryFixture
import com.jorhetfield.atelier.data.mocks.artworkJson
import com.jorhetfield.atelier.data.mocks.searchJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import mockwebserver3.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class ArtworkRepositoryCancellationTest {
    private val fixture = RepositoryFixture()
    private val repository = fixture.repository

    @AfterEach
    fun tearDown() = fixture.close()

    @Test
    fun `a cancelled search is not cached, so the next one goes to the network`() =
        runTest {
            fixture.server.enqueue(
                MockResponse
                    .Builder()
                    .body(searchJson(artworkJson(1, title = "Stale")))
                    .headersDelay(2, TimeUnit.SECONDS)
                    .build(),
            )

            val inFlight = launch { repository.search("monet", 1) }
            runCurrent()
            val reachedServer = withContext(Dispatchers.IO) { fixture.server.takeRequest(5, TimeUnit.SECONDS) }
            assertNotNull(reachedServer)

            inFlight.cancelAndJoin()
            assertTrue(inFlight.isCancelled)

            fixture.enqueue(searchJson(artworkJson(2, title = "Fresh")))
            val result = repository.search("monet", 1)

            assertEquals("Fresh", result.artworks.single().title)
            assertEquals(2, fixture.server.requestCount)
        }
}

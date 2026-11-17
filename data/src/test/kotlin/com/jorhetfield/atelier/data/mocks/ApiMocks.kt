package com.jorhetfield.atelier.data.mocks

import com.jorhetfield.atelier.data.buildArtworkRepository
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TestTimeSource

class RepositoryFixture : AutoCloseable {
    val server = MockWebServer().apply { start() }
    val time = TestTimeSource()
    val repository = buildArtworkRepository(server.url("/api/v1/"), time, cacheTtl = 3.minutes)

    fun enqueue(
        body: String,
        code: Int = 200,
    ) = server.enqueue(
        MockResponse
            .Builder()
            .code(code)
            .body(body)
            .build(),
    )

    override fun close() = server.close()
}

fun artworkJson(
    id: Int,
    title: String? = "Artwork $id",
    score: Double? = 100.0,
) = buildString {
    append("""{"id":$id""")
    if (title != null) append(""","title":"$title"""")
    if (score != null) append(""","_score":$score""")
    append("}")
}

fun searchJson(vararg artworks: String) = """{"data":[${artworks.joinToString(",")}]}"""

fun detailJson(artwork: String) = """{"data":$artwork}"""

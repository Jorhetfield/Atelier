package com.jorhetfield.atelier.mocks

import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.repository.ArtworkRepository

class FakeArtworkRepository : ArtworkRepository {
    var onSearch: suspend (String) -> List<Artwork> = { emptyList() }
    var onGetArtwork: suspend (Int) -> Artwork = { error("getArtwork was not stubbed") }
    var onGetHighlights: suspend () -> List<Artwork> = { emptyList() }

    val searches = mutableListOf<String>()
    var highlightsCalls = 0
        private set

    override suspend fun search(query: String): List<Artwork> {
        searches += query
        return onSearch(query)
    }

    override suspend fun getArtwork(id: Int): Artwork = onGetArtwork(id)

    override suspend fun getHighlights(): List<Artwork> {
        highlightsCalls++
        return onGetHighlights()
    }
}

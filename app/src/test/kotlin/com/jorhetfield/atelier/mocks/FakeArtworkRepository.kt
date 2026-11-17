package com.jorhetfield.atelier.mocks

import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.repository.ArtworkRepository

class FakeArtworkRepository : ArtworkRepository {
    var onSearch: suspend (String) -> List<Artwork> = { emptyList() }
    var onGetArtwork: suspend (Int) -> Artwork = { error("getArtwork was not stubbed") }

    val searches = mutableListOf<String>()

    override suspend fun search(query: String): List<Artwork> {
        searches += query
        return onSearch(query)
    }

    override suspend fun getArtwork(id: Int): Artwork = onGetArtwork(id)
}

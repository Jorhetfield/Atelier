package com.jorhetfield.atelier.mocks

import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.model.SearchPage
import com.jorhetfield.atelier.domain.repository.ArtworkRepository

class FakeArtworkRepository : ArtworkRepository {
    // Most tests only care about the artworks of a first page; paging tests override onSearchPage.
    var onSearch: suspend (String) -> List<Artwork> = { emptyList() }
    var onSearchPage: suspend (query: String, page: Int) -> SearchPage =
        { query, _ -> SearchPage(onSearch(query), hasMore = false) }
    var onGetArtwork: suspend (Int) -> Artwork = { error("getArtwork was not stubbed") }
    var onGetHighlights: suspend () -> List<Artwork> = { emptyList() }

    val searchRequests = mutableListOf<Pair<String, Int>>()
    val searches: List<String> get() = searchRequests.map { it.first }
    var highlightsCalls = 0
        private set

    override suspend fun search(
        query: String,
        page: Int,
    ): SearchPage {
        searchRequests += query to page
        return onSearchPage(query, page)
    }

    override suspend fun getArtwork(id: Int): Artwork = onGetArtwork(id)

    override suspend fun getHighlights(): List<Artwork> {
        highlightsCalls++
        return onGetHighlights()
    }
}

package com.jorhetfield.atelier.data

import com.jorhetfield.atelier.data.cache.TtlCache
import com.jorhetfield.atelier.data.remote.ArtworkApi
import com.jorhetfield.atelier.data.remote.toDomain
import com.jorhetfield.atelier.data.remote.toHighlightsPage
import com.jorhetfield.atelier.data.remote.toSearchPage
import com.jorhetfield.atelier.data.remote.translatingErrors
import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.model.SearchPage
import com.jorhetfield.atelier.domain.repository.ArtworkRepository

// Every page of a query is cached on its own, so scrolling back never repeats a request.
internal data class SearchKey(
    val query: String,
    val page: Int,
)

internal class ArtworkRepositoryImpl(
    private val api: ArtworkApi,
    private val searchCache: TtlCache<SearchKey, SearchPage>,
    private val artworkCache: TtlCache<Int, Artwork>,
    private val highlightsCache: TtlCache<Int, SearchPage>,
) : ArtworkRepository {
    override suspend fun search(
        query: String,
        page: Int,
    ): SearchPage {
        val key = SearchKey(query.trim().lowercase(), page)
        searchCache.get(key)?.let { return it }

        // The cache lock is not held while waiting on the network, so searches never block each other.
        val result = translatingErrors { api.search(key.query, page) }.toSearchPage(page)
        searchCache.put(key, result)
        artworkCache.putAll(result.artworks.associateBy { it.id })
        return result
    }

    override suspend fun getArtwork(id: Int): Artwork {
        artworkCache.get(id)?.let { return it }

        val artwork = translatingErrors { api.artwork(id) }.data.toDomain()
        artworkCache.put(id, artwork)
        return artwork
    }

    override suspend fun getHighlights(page: Int): SearchPage {
        highlightsCache.get(page)?.let { return it }

        val result = translatingErrors { api.highlights(page = page) }.toHighlightsPage()
        highlightsCache.put(page, result)
        artworkCache.putAll(result.artworks.associateBy { it.id })
        return result
    }
}

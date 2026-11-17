package com.jorhetfield.atelier.data

import com.jorhetfield.atelier.data.cache.TtlCache
import com.jorhetfield.atelier.data.remote.ArtworkApi
import com.jorhetfield.atelier.data.remote.toArtworks
import com.jorhetfield.atelier.data.remote.toDomain
import com.jorhetfield.atelier.data.remote.toHighlights
import com.jorhetfield.atelier.data.remote.translatingErrors
import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.repository.ArtworkRepository

internal class ArtworkRepositoryImpl(
    private val api: ArtworkApi,
    private val searchCache: TtlCache<String, List<Artwork>>,
    private val artworkCache: TtlCache<Int, Artwork>,
    private val highlightsCache: TtlCache<Unit, List<Artwork>>,
) : ArtworkRepository {
    override suspend fun search(query: String): List<Artwork> {
        val key = query.trim().lowercase()
        searchCache.get(key)?.let { return it }

        // The cache lock is not held while waiting on the network, so searches never block each other.
        val artworks = translatingErrors { api.search(key) }.toArtworks()
        searchCache.put(key, artworks)
        artworkCache.putAll(artworks.associateBy { it.id })
        return artworks
    }

    override suspend fun getArtwork(id: Int): Artwork {
        artworkCache.get(id)?.let { return it }

        val artwork = translatingErrors { api.artwork(id) }.data.toDomain()
        artworkCache.put(id, artwork)
        return artwork
    }

    override suspend fun getHighlights(): List<Artwork> {
        highlightsCache.get(Unit)?.let { return it }

        val artworks = translatingErrors { api.highlights() }.toHighlights()
        highlightsCache.put(Unit, artworks)
        artworkCache.putAll(artworks.associateBy { it.id })
        return artworks
    }
}

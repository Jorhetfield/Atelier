package com.jorhetfield.atelier.domain.repository

import com.jorhetfield.atelier.domain.model.Artwork

public interface ArtworkRepository {
    public suspend fun search(query: String): List<Artwork>

    public suspend fun getArtwork(id: Int): Artwork

    public suspend fun getHighlights(): List<Artwork>
}

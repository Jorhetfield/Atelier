package com.jorhetfield.atelier.domain.repository

import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.model.SearchPage

public interface ArtworkRepository {
    // Pages start at 1.
    public suspend fun search(
        query: String,
        page: Int,
    ): SearchPage

    public suspend fun getArtwork(id: Int): Artwork

    public suspend fun getHighlights(): List<Artwork>
}

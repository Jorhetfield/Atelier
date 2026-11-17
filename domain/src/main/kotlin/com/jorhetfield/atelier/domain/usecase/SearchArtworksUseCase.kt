package com.jorhetfield.atelier.domain.usecase

import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.repository.ArtworkRepository

public class SearchArtworksUseCase(
    private val repository: ArtworkRepository,
) {
    public suspend operator fun invoke(query: String): List<Artwork> {
        val trimmed = query.trim()
        // The API answers a blank query with random artworks, so don't spend a request on it.
        if (trimmed.isEmpty()) return emptyList()
        return repository.search(trimmed)
    }
}

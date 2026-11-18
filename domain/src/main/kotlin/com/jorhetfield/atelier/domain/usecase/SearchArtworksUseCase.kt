package com.jorhetfield.atelier.domain.usecase

import com.jorhetfield.atelier.domain.model.SearchPage
import com.jorhetfield.atelier.domain.repository.ArtworkRepository

public class SearchArtworksUseCase(
    private val repository: ArtworkRepository,
) {
    public suspend operator fun invoke(
        query: String,
        page: Int = 1,
    ): SearchPage {
        require(page >= 1) { "Pages start at 1, got $page" }
        val trimmed = query.trim()
        // The API answers a blank query with random artworks, so don't spend a request on it.
        if (trimmed.isEmpty()) return SearchPage.Empty
        return repository.search(trimmed, page)
    }
}

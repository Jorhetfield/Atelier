package com.jorhetfield.atelier.domain.usecase

import com.jorhetfield.atelier.domain.model.SearchPage
import com.jorhetfield.atelier.domain.repository.ArtworkRepository

public class GetHighlightsUseCase(
    private val repository: ArtworkRepository,
) {
    public suspend operator fun invoke(page: Int = 1): SearchPage {
        require(page >= 1) { "Pages start at 1, got $page" }
        return repository.getHighlights(page)
    }
}

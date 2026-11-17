package com.jorhetfield.atelier.domain.usecase

import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.repository.ArtworkRepository

public class GetHighlightsUseCase(
    private val repository: ArtworkRepository,
) {
    public suspend operator fun invoke(): List<Artwork> = repository.getHighlights()
}

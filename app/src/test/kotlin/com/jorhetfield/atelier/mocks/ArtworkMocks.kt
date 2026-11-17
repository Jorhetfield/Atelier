package com.jorhetfield.atelier.mocks

import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.model.ArtworkImage

fun artwork(
    id: Int = 1,
    title: String = "Artwork $id",
    image: ArtworkImage? = null,
) = Artwork(
    id = id,
    title = title,
    artist = "Georges Seurat",
    date = "1884",
    medium = "Oil on canvas",
    origin = "France",
    dimensions = "207.5 x 308.1 cm",
    classification = "painting",
    style = "Post-Impressionism",
    isPublicDomain = true,
    image = image,
    dominantColor = null,
)

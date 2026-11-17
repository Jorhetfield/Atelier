package com.jorhetfield.atelier.domain.mocks

import com.jorhetfield.atelier.domain.model.Artwork

fun artwork(
    id: Int = 1,
    title: String = "A Sunday on La Grande Jatte",
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
    image = null,
    dominantColor = null,
)

package com.jorhetfield.atelier.domain.model

public data class Artwork(
    val id: Int,
    val title: String,
    val artist: String?,
    val date: String?,
    val medium: String?,
    val origin: String?,
    val dimensions: String?,
    val classification: String?,
    val style: String?,
    val isPublicDomain: Boolean,
    val image: ArtworkImage?,
    val dominantColor: DominantColor?,
)

// No URL here: the size depends on the screen, so the UI builds it from the id.
public data class ArtworkImage(
    val id: String,
    val altText: String?,
    val placeholder: String?,
    val width: Int?,
    val height: Int?,
)

// HSL as the API gives it: hue 0-360, saturation and lightness 0-100.
public data class DominantColor(
    val hue: Int,
    val saturation: Int,
    val lightness: Int,
)

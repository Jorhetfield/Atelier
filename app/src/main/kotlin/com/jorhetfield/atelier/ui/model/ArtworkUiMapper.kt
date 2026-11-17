package com.jorhetfield.atelier.ui.model

import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.model.ArtworkImage

private const val DEFAULT_ASPECT_RATIO = 4f / 3f

fun Artwork.toUi(): ArtworkUi =
    ArtworkUi(
        id = id,
        title = title,
        artist = artist,
        image = image?.toUi(),
    )

fun Artwork.toDetailUi(): ArtworkDetailUi =
    ArtworkDetailUi(
        id = id,
        title = title,
        artist = artist,
        date = date,
        medium = medium,
        origin = origin,
        dimensions = dimensions,
        classification = classification,
        style = style,
        isPublicDomain = isPublicDomain,
        image = image?.toUi(),
        dominantColor = dominantColor,
    )

// IIIF serves any width on request, so the screen picks the size instead of the data layer.
fun ArtworkImageUi.url(width: Int): String = "https://www.artic.edu/iiif/2/$id/full/$width,/0/default.jpg"

private fun ArtworkImage.toUi(): ArtworkImageUi {
    val imageWidth = width
    val imageHeight = height
    val ratio =
        if (imageWidth != null && imageHeight != null && imageWidth > 0 && imageHeight > 0) {
            imageWidth.toFloat() / imageHeight
        } else {
            DEFAULT_ASPECT_RATIO
        }
    return ArtworkImageUi(id = id, contentDescription = altText, placeholder = placeholder, aspectRatio = ratio)
}

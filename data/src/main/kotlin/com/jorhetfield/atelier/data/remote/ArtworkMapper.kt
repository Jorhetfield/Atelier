package com.jorhetfield.atelier.data.remote

import com.jorhetfield.atelier.domain.model.Artwork
import com.jorhetfield.atelier.domain.model.ArtworkImage
import com.jorhetfield.atelier.domain.model.DominantColor
import com.jorhetfield.atelier.domain.model.SearchPage

// The API ranks the whole catalog for any query and gives non-matches a score around 0.00002;
// real matches start well above 1. Anything under this is noise, not a result.
internal const val MIN_RELEVANCE = 0.01

internal fun SearchResponseDto.toArtworks(): List<Artwork> =
    data
        .filter { it.score == null || it.score >= MIN_RELEVANCE }
        .map { it.toDomain() }

// Results come ranked, so the relevant ones are always first and the noise after them. A full page
// of relevant results may be followed by more; any noise, or a short page, means this was the last.
internal fun SearchResponseDto.toSearchPage(page: Int): SearchPage {
    val relevant = toArtworks()
    val endedHere = data.size < PAGE_SIZE || relevant.size < data.size
    return SearchPage(relevant, hasMore = !endedHere && page < MAX_PAGE)
}

// A filter query has no text relevance, so there is no score to judge by and nothing to drop.
internal fun SearchResponseDto.toHighlights(): List<Artwork> = data.map { it.toDomain() }

internal fun ArtworkDto.toDomain(): Artwork =
    Artwork(
        id = id,
        title = title.orEmptyToNull() ?: "Untitled",
        artist = artistDisplay?.lineSequence()?.firstOrNull().orEmptyToNull(),
        date = dateDisplay.orEmptyToNull(),
        medium = mediumDisplay.orEmptyToNull(),
        origin = placeOfOrigin.orEmptyToNull(),
        dimensions = dimensions.orEmptyToNull(),
        classification = classificationTitle.orEmptyToNull(),
        style = styleTitle.orEmptyToNull(),
        isPublicDomain = isPublicDomain ?: false,
        image =
            imageId.orEmptyToNull()?.let {
                ArtworkImage(
                    id = it,
                    altText = thumbnail?.altText.orEmptyToNull(),
                    placeholder = thumbnail?.lqip.orEmptyToNull(),
                    width = thumbnail?.width,
                    height = thumbnail?.height,
                )
            },
        dominantColor = color?.toDomain(),
    )

private fun ColorDto.toDomain(): DominantColor? {
    if (h == null || s == null || l == null) return null
    return DominantColor(hue = h, saturation = s, lightness = l)
}

private fun String?.orEmptyToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

package com.jorhetfield.atelier.domain.model

// One page of a search. `hasMore` is decided by the data source, which is the only one that knows
// how the API ends its results, so callers never have to guess from the page size.
public data class SearchPage(
    val artworks: List<Artwork>,
    val hasMore: Boolean,
) {
    public companion object {
        public val Empty: SearchPage = SearchPage(emptyList(), hasMore = false)
    }
}

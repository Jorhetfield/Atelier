package com.jorhetfield.atelier.ui.home

import androidx.annotation.StringRes
import com.jorhetfield.atelier.R

sealed interface ShelfSource {
    data object Highlights : ShelfSource

    data class Search(
        val query: String,
    ) : ShelfSource
}

// The curated rows of the home screen, in the order they appear. Artists work better than concepts here:
// the museum holds famous pieces by each of them, so a plain search already returns the right ones.
enum class HomeShelf(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val source: ShelfSource,
) {
    Highlights(R.string.shelf_highlights_title, R.string.shelf_highlights_subtitle, ShelfSource.Highlights),
    Monet(R.string.shelf_monet_title, R.string.shelf_monet_subtitle, ShelfSource.Search("monet")),
    VanGogh(R.string.shelf_van_gogh_title, R.string.shelf_van_gogh_subtitle, ShelfSource.Search("van gogh")),
    Picasso(R.string.shelf_picasso_title, R.string.shelf_picasso_subtitle, ShelfSource.Search("picasso")),
    JapanesePrints(
        R.string.shelf_japanese_prints_title,
        R.string.shelf_japanese_prints_subtitle,
        ShelfSource.Search("japanese prints"),
    ),
}

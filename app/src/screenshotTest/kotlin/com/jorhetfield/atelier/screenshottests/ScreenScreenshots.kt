package com.jorhetfield.atelier.screenshottests

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.jorhetfield.atelier.designsystem.preview.AtelierScreenPreviews
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme
import com.jorhetfield.atelier.domain.model.DominantColor
import com.jorhetfield.atelier.ui.detail.DetailScreenContent
import com.jorhetfield.atelier.ui.detail.DetailUiState
import com.jorhetfield.atelier.ui.home.HomeShelf
import com.jorhetfield.atelier.ui.home.HomeUiState
import com.jorhetfield.atelier.ui.home.ShelfContent
import com.jorhetfield.atelier.ui.home.ShelfUi
import com.jorhetfield.atelier.ui.model.ArtworkDetailUi
import com.jorhetfield.atelier.ui.model.ArtworkImageUi
import com.jorhetfield.atelier.ui.model.ArtworkUi
import com.jorhetfield.atelier.ui.model.ErrorKind
import com.jorhetfield.atelier.ui.search.SearchContent
import com.jorhetfield.atelier.ui.search.SearchScreenContent
import com.jorhetfield.atelier.ui.search.SearchUiState

private val titles =
    listOf(
        "A Sunday on La Grande Jatte",
        "Water Lilies",
        "Nighthawks",
        "The Bedroom",
        "Self-Portrait with a Very Long Title That Needs to Wrap",
        "Stacks of Wheat",
        "Two Sisters",
        "Paris Street; Rainy Day",
    )

// Varied hues show how each card takes the color of its own artwork. No placeholder or url is real.
private val hues = listOf(205, 30, 120, 340, 55, 260, 170, 15)

private val sampleArtworks =
    titles.mapIndexed { index, title ->
        ArtworkUi(
            id = index,
            title = title,
            artist = "Georges Seurat",
            image =
                ArtworkImageUi(
                    id = "sample-$index",
                    contentDescription = null,
                    placeholder = null,
                    aspectRatio = if (index % 2 == 0) 1.3f else 0.8f,
                    dominantColor = DominantColor(hue = hues[index], saturation = 45, lightness = 50),
                ),
        )
    }

private val sampleDetail =
    ArtworkDetailUi(
        id = 27992,
        title = "A Sunday on La Grande Jatte",
        artist = "Georges Seurat (French, 1859-1891)",
        date = "1884-86",
        medium = "Oil on canvas",
        origin = "France",
        dimensions = "207.5 x 308.1 cm (81 3/4 x 121 1/4 in.)",
        classification = "oil on canvas",
        style = "Pointillism",
        isPublicDomain = true,
        image = null,
        dominantColor = DominantColor(hue = 59, saturation = 12, lightness = 52),
    )

// Three of the shelves with pictures, one still loading and one that failed (and so is left out).
private val sampleHome =
    HomeUiState(
        shelves =
            listOf(
                ShelfUi(HomeShelf.Highlights, ShelfContent.Loaded(sampleArtworks)),
                ShelfUi(HomeShelf.Monet, ShelfContent.Loaded(sampleArtworks.reversed())),
                ShelfUi(HomeShelf.VanGogh, ShelfContent.Loading),
                ShelfUi(HomeShelf.Picasso, ShelfContent.Failed(ErrorKind.Generic)),
            ),
    )

@Composable
private fun Search(
    content: SearchContent,
    query: String,
    home: HomeUiState = sampleHome,
) {
    AtelierTheme(dynamicColor = false) {
        SearchScreenContent(
            state = SearchUiState(query = query, content = content),
            home = home,
            onQueryChange = {},
            onSubmit = {},
            onRetry = {},
            onHomeRetry = {},
            onLoadMore = {},
            onArtworkClick = {},
        )
    }
}

@Composable
private fun Detail(state: DetailUiState) {
    AtelierTheme(dynamicColor = false) {
        DetailScreenContent(state = state, onBack = {}, onViewArtwork = {}, onRetry = {})
    }
}

@PreviewTest
@AtelierScreenPreviews
@Composable
fun SearchIdleScreenshot() = Search(SearchContent.Idle, query = "")

@PreviewTest
@AtelierScreenPreviews
@Composable
fun SearchResultsScreenshot() = Search(SearchContent.Results(sampleArtworks), query = "seurat")

@PreviewTest
@AtelierScreenPreviews
@Composable
fun SearchNoResultsScreenshot() = Search(SearchContent.NoResults("qwertyuiop"), query = "qwertyuiop")

@PreviewTest
@AtelierScreenPreviews
@Composable
fun SearchErrorScreenshot() = Search(SearchContent.Failure(ErrorKind.Offline), query = "seurat")

@PreviewTest
@AtelierScreenPreviews
@Composable
fun DetailScreenshot() = Detail(DetailUiState.Success(sampleDetail))

@PreviewTest
@AtelierScreenPreviews
@Composable
fun DetailErrorScreenshot() = Detail(DetailUiState.Failure(ErrorKind.NotFound))

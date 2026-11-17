package com.jorhetfield.atelier.screenshottests

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.jorhetfield.atelier.designsystem.preview.AtelierScreenPreviews
import com.jorhetfield.atelier.designsystem.theme.AtelierTheme
import com.jorhetfield.atelier.ui.detail.DetailScreenContent
import com.jorhetfield.atelier.ui.detail.DetailUiState
import com.jorhetfield.atelier.ui.model.ArtworkDetailUi
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

private val sampleArtworks =
    titles.mapIndexed { index, title ->
        ArtworkUi(id = index, title = title, artist = "Georges Seurat", image = null)
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
        dominantColor = null,
    )

@Composable
private fun Search(
    content: SearchContent,
    query: String,
) {
    AtelierTheme(dynamicColor = false) {
        SearchScreenContent(
            state = SearchUiState(query = query, content = content),
            onQueryChange = {},
            onSubmit = {},
            onRetry = {},
            onArtworkClick = {},
        )
    }
}

@Composable
private fun Detail(state: DetailUiState) {
    AtelierTheme(dynamicColor = false) {
        DetailScreenContent(state = state, onBack = {}, onRetry = {})
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

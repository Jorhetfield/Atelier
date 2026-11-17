package com.jorhetfield.atelier.ui.detail

import com.jorhetfield.atelier.ui.model.ArtworkDetailUi
import com.jorhetfield.atelier.ui.model.ErrorKind

sealed interface DetailUiState {
    data object Loading : DetailUiState

    data class Success(
        val artwork: ArtworkDetailUi,
    ) : DetailUiState

    data class Failure(
        val error: ErrorKind,
    ) : DetailUiState
}

package com.jorhetfield.atelier.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorhetfield.atelier.domain.error.ArtworkException
import com.jorhetfield.atelier.domain.usecase.GetArtworkUseCase
import com.jorhetfield.atelier.ui.model.toDetailUi
import com.jorhetfield.atelier.ui.model.toErrorKind
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel
    @AssistedInject
    constructor(
        @Assisted private val artworkId: Int,
        private val getArtwork: GetArtworkUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
        val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

        private var loadJob: Job? = null

        init {
            load()
        }

        fun retry() = load()

        // viewModelScope runs on Main.immediate, so a cached artwork (the repository answers without
        // suspending) is published before the first frame: Loading is only ever seen when there is
        // a real wait. The state also lives in the view model, not in a stateIn that restarts after
        // the screen has been off for a while, so coming back from the viewer never flashes Loading.
        private fun load() {
            loadJob?.cancel()
            _uiState.value = DetailUiState.Loading
            loadJob =
                viewModelScope.launch {
                    _uiState.value =
                        try {
                            DetailUiState.Success(getArtwork(artworkId).toDetailUi())
                        } catch (e: ArtworkException) {
                            DetailUiState.Failure(e.toErrorKind())
                        }
                }
        }

        @AssistedFactory
        interface Factory {
            fun create(artworkId: Int): DetailViewModel
        }
    }

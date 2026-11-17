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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel
    @AssistedInject
    constructor(
        @Assisted private val artworkId: Int,
        private val getArtwork: GetArtworkUseCase,
    ) : ViewModel() {
        private val retries = Channel<Unit>(Channel.CONFLATED)

        val uiState: StateFlow<DetailUiState> =
            retries
                .receiveAsFlow()
                .onStart { emit(Unit) }
                .flatMapLatest { load() }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState.Loading)

        fun retry() {
            retries.trySend(Unit)
        }

        private fun load(): Flow<DetailUiState> =
            flow {
                emit(DetailUiState.Loading)
                emit(
                    try {
                        DetailUiState.Success(getArtwork(artworkId).toDetailUi())
                    } catch (e: ArtworkException) {
                        DetailUiState.Failure(e.toErrorKind())
                    },
                )
            }

        @AssistedFactory
        interface Factory {
            fun create(artworkId: Int): DetailViewModel
        }
    }

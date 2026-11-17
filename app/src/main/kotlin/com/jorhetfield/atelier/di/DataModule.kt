package com.jorhetfield.atelier.di

import com.jorhetfield.atelier.data.createArtworkRepository
import com.jorhetfield.atelier.domain.repository.ArtworkRepository
import com.jorhetfield.atelier.domain.usecase.GetArtworkUseCase
import com.jorhetfield.atelier.domain.usecase.GetHighlightsUseCase
import com.jorhetfield.atelier.domain.usecase.SearchArtworksUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    // Singleton on purpose: the in-memory cache lives in the repository and must outlive every screen.
    @Provides
    @Singleton
    fun provideArtworkRepository(): ArtworkRepository = createArtworkRepository()

    @Provides
    fun provideSearchArtworks(repository: ArtworkRepository) = SearchArtworksUseCase(repository)

    @Provides
    fun provideGetArtwork(repository: ArtworkRepository) = GetArtworkUseCase(repository)

    @Provides
    fun provideGetHighlights(repository: ArtworkRepository) = GetHighlightsUseCase(repository)
}

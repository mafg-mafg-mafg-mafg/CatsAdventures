package com.mafg.mafg.mafg.mafg.catsadventures.di

import com.mafg.mafg.mafg.mafg.catsadventures.data.repository.GameRepositoryImpl
import com.mafg.mafg.mafg.mafg.catsadventures.domain.repository.GameRepository
import com.mafg.mafg.mafg.mafg.catsadventures.domain.usecase.NavigateUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGameRepository(): GameRepository = GameRepositoryImpl()

    @Provides
    @Singleton
    fun provideNavigateUseCase(repository: GameRepository): NavigateUseCase {
        return NavigateUseCase(repository)
    }
}
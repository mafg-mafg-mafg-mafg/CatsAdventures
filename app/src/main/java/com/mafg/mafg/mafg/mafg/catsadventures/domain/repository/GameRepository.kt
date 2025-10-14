package com.mafg.mafg.mafg.mafg.catsadventures.domain.repository

import com.mafg.mafg.mafg.mafg.catsadventures.domain.model.GameState

interface GameRepository {
    suspend fun navigateToNextScreen(currentState: GameState): GameState
    fun getInitialState(): GameState
}
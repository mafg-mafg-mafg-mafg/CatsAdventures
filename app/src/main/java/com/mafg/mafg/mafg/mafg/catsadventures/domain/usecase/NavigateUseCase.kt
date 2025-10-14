package com.mafg.mafg.mafg.mafg.catsadventures.domain.usecase

import com.mafg.mafg.mafg.mafg.catsadventures.domain.model.GameState
import com.mafg.mafg.mafg.mafg.catsadventures.domain.repository.GameRepository

class NavigateUseCase(private val repository: GameRepository) {
    suspend fun execute(currentState: GameState): GameState {
        return repository.navigateToNextScreen(currentState)
    }
}
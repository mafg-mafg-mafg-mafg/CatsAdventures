package com.mafg.mafg.mafg.mafg.catsadventures.data.repository

import com.mafg.mafg.mafg.mafg.catsadventures.domain.model.GameState
import com.mafg.mafg.mafg.mafg.catsadventures.domain.model.Screen
import com.mafg.mafg.mafg.mafg.catsadventures.domain.repository.GameRepository
import kotlinx.coroutines.delay

class GameRepositoryImpl : GameRepository {
    override suspend fun navigateToNextScreen(currentState: GameState): GameState {
        return when (currentState.currentScreen) {
            is Screen.Main -> currentState.copy(
                currentScreen = Screen.Chapter(1),
                currentChapter = 1
            )
            is Screen.Chapter -> {
                delay(3000) // Simula carga
                currentState.copy(currentScreen = Screen.Canvas)
            }
            is Screen.Canvas -> currentState.copy(currentScreen = Screen.Main)
        }
    }

    override fun getInitialState(): GameState = GameState()
}
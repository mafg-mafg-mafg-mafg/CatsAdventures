package com.mafg.mafg.mafg.mafg.catsadventures.domain.model

sealed class Screen {
    object Main : Screen()
    data class Chapter(val number: Int) : Screen()
    object Canvas : Screen()
}

data class GameState(
    val currentScreen: Screen = Screen.Canvas,
    val currentChapter: Int = 1,
    val isLoading: Boolean = false,
    val score: Int = 10,
    val timeLeft: Float = 50f,
    val isRainbowActive: Boolean = false
)

package com.mafg.mafg.mafg.mafg.catsadventures.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mafg.mafg.mafg.mafg.catsadventures.domain.model.Screen
import com.mafg.mafg.mafg.mafg.catsadventures.presentation.viewmodel.GameEvent
import com.mafg.mafg.mafg.mafg.catsadventures.presentation.viewmodel.GameViewModel

@Composable
fun MainScreen(
    viewModel: GameViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        when (val screen = gameState.currentScreen) {
            is Screen.Main -> PlayButton(
                modifier = Modifier.padding(innerPadding),
                onPlayClick = { viewModel.onEvent(GameEvent.Navigate) }
            )
            is Screen.Chapter -> ChapterScreen(
                chapterNumber = screen.number,
                modifier = Modifier.padding(innerPadding)
            )
            is Screen.Canvas -> DrawingCanvas(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
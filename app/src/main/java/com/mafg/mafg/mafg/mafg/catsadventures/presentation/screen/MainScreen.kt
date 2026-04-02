package com.mafg.mafg.mafg.mafg.catsadventures.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mafg.mafg.mafg.mafg.catsadventures.presentation.viewmodel.GameViewModel

@Composable
fun MainScreen(
    viewModel: GameViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        DrawingCanvas(
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel
        )
    }
}

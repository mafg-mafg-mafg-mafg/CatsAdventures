package com.mafg.mafg.mafg.mafg.catsadventures.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mafg.mafg.mafg.mafg.catsadventures.presentation.component.GameCanvas
import com.mafg.mafg.mafg.mafg.catsadventures.presentation.viewmodel.GameViewModel

@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = hiltViewModel()
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GameCanvas(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel
        )
    }
}

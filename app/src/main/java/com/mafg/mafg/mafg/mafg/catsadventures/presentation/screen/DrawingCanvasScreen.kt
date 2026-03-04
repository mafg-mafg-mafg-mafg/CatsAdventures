package com.mafg.mafg.mafg.mafg.catsadventures.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mafg.mafg.mafg.mafg.catsadventures.presentation.component.GameCanvas

@Composable
fun DrawingCanvas(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GameCanvas()
    }
}
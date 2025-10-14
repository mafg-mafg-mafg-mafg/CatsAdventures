package com.mafg.mafg.mafg.mafg.catsadventures.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mafg.mafg.mafg.mafg.catsadventures.presentation.viewmodel.GameEvent
import com.mafg.mafg.mafg.mafg.catsadventures.presentation.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun ChapterScreen(
    chapterNumber: Int,
    modifier: Modifier = Modifier
) {
    val viewModel: GameViewModel = hiltViewModel()

    LaunchedEffect(key1 = chapterNumber) {
        delay(3000)
        viewModel.onEvent(GameEvent.Navigate)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Capítulo $chapterNumber",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
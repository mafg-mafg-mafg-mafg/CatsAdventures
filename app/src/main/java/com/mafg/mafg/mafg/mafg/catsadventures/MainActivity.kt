package com.mafg.mafg.mafg.mafg.catsadventures

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mafg.mafg.mafg.mafg.catsadventures.presentation.screen.MainScreen
import com.mafg.mafg.mafg.mafg.catsadventures.presentation.viewmodel.GameViewModel
import com.mafg.mafg.mafg.mafg.catsadventures.ui.theme.CatsAdventuresTheme
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Eliminamos enableEdgeToEdge() para tener control total desde el Theme
        setContent {
            CatsAdventuresTheme {
                val viewModel: GameViewModel = hiltViewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
package com.mafg.mafg.mafg.mafg.catsadventures.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mafg.mafg.mafg.mafg.catsadventures.domain.model.GameState
import com.mafg.mafg.mafg.mafg.catsadventures.domain.model.Screen
import com.mafg.mafg.mafg.mafg.catsadventures.domain.usecase.NavigateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    // OPCIÓN 1: Usando StateFlow (Recomendado para ViewModel)
    private val _gameState = MutableStateFlow(GameState(currentScreen = Screen.Main))
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // OPCIÓN 2: O mantener tu approach pero cambiar el tipo
    // private val _gameState = mutableStateOf(GameState())
    // val gameState: State<GameState> = _gameState



    fun onEvent(event: GameEvent) {
        viewModelScope.launch {
            when (event) {
                is GameEvent.Navigate -> {
                    _gameState.value = navigateUseCase.execute(_gameState.value)
                }
                is GameEvent.Reset -> {
                    _gameState.value = GameState()
                }
            }
        }
    }
}

sealed class GameEvent {
    object Navigate : GameEvent()
    object Reset : GameEvent()
}
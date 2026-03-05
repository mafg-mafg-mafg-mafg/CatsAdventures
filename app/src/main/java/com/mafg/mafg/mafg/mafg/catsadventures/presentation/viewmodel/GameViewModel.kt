package com.mafg.mafg.mafg.mafg.catsadventures.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mafg.mafg.mafg.mafg.catsadventures.domain.model.GameState
import com.mafg.mafg.mafg.mafg.catsadventures.domain.model.Screen
import com.mafg.mafg.mafg.mafg.catsadventures.domain.usecase.NavigateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState(currentScreen = Screen.Main))
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.Navigate -> {
                viewModelScope.launch {
                    _gameState.value = navigateUseCase.execute(_gameState.value)
                }
            }
            is GameEvent.Reset -> {
                _gameState.value = GameState()
            }
            is GameEvent.DecrementScore -> {
                if (_gameState.value.score > 0) {
                    _gameState.update { it.copy(score = it.score - 1, isRainbowActive = true) }
                    // Desactivar arcoiris tras 3 segundos
                    viewModelScope.launch {
                        delay(3000)
                        _gameState.update { it.copy(isRainbowActive = false) }
                    }
                }
            }
            is GameEvent.DecrementTime -> {
                if (_gameState.value.timeLeft > 0f) {
                    // Decrementamos en pasos más pequeños para los milisegundos (ej. 0.01s)
                    _gameState.update { it.copy(timeLeft = (it.timeLeft - 0.01f).coerceAtLeast(0f)) }
                }
            }
        }
    }
}

sealed class GameEvent {
    object Navigate : GameEvent()
    object Reset : GameEvent()
    object DecrementScore : GameEvent()
    object DecrementTime : GameEvent()
}
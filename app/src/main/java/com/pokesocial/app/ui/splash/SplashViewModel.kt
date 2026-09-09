package com.pokesocial.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokesocial.app.data.local.SeedManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SplashState(
    val message: String = "Preparando…",
    val progress: Float = 0f,
    val ready: Boolean = false,
    val error: String? = null
)

class SplashViewModel(
    private val seedManager: SeedManager
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                if (seedManager.needsSeed()) {
                    seedManager.seed { p ->
                        _state.value = SplashState(p.step, p.fraction, ready = false)
                    }
                } else {
                    _state.value = SplashState("Abrindo feed…", 1f, ready = false)
                }
                _state.value = SplashState("Pronto", 1f, ready = true)
            }.onFailure { e ->
                _state.value = SplashState(
                    message = "Falha ao carregar",
                    progress = 0f,
                    ready = false,
                    error = e.message ?: "Erro desconhecido"
                )
            }
        }
    }

    fun retry() {
        _state.value = SplashState()
        viewModelScope.launch {
            // destructive: clear meta by reseeding only if needed - force by deleting meta is complex;
            // just retry seed path
            runCatching {
                seedManager.seed { p ->
                    _state.value = SplashState(p.step, p.fraction)
                }
                _state.value = SplashState("Pronto", 1f, ready = true)
            }.onFailure { e ->
                _state.value = SplashState(error = e.message, message = "Falha ao carregar")
            }
        }
    }
}

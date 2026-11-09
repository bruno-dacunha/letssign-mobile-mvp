package br.com.fiap.letssign.presentation.autenticacao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.usecase.autenticacao.RealizarLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val realizarLoginUseCase: RealizarLoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun aoAlterarEmail(novoEmail: String) {
        _uiState.update { it.copy(email = novoEmail, mensagemErro = null) }
    }

    fun aoAlterarSenha(novaSenha: String) {
        _uiState.update { it.copy(senha = novaSenha, mensagemErro = null) }
    }

    fun alternarVisibilidadeSenha() {
        _uiState.update { it.copy(senhaVisivel = !it.senhaVisivel) }
    }

    fun preencherCredencialDemonstracao(email: String, senha: String) {
        _uiState.update {
            it.copy(
                email = email,
                senha = senha,
                mensagemErro = null
            )
        }
    }

    fun realizarLogin() {
        val estadoAtual = _uiState.value
        if (estadoAtual.carregando) return

        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, mensagemErro = null) }

            when (val resultado = realizarLoginUseCase(estadoAtual.email, estadoAtual.senha)) {
                is ResultadoOperacao.Sucesso -> {
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            usuarioAutenticado = resultado.dados
                        )
                    }
                }
                is ResultadoOperacao.Erro -> {
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            mensagemErro = resultado.mensagem
                        )
                    }
                }
            }
        }
    }

    fun consumirNavegacaoPosLogin() {
        _uiState.update { it.copy(usuarioAutenticado = null) }
    }
}

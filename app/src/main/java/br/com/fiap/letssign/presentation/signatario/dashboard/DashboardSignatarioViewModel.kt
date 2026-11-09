package br.com.fiap.letssign.presentation.signatario.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.model.StatusAssinatura
import br.com.fiap.letssign.domain.repository.SignatarioRepository
import br.com.fiap.letssign.domain.usecase.autenticacao.RealizarLogoutUseCase
import br.com.fiap.letssign.domain.usecase.documento.ObterDashboardSignatarioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardSignatarioViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val signatarioRepository: SignatarioRepository,
    private val obterDashboardSignatarioUseCase: ObterDashboardSignatarioUseCase,
    private val realizarLogoutUseCase: RealizarLogoutUseCase
) : ViewModel() {

    val signatarioId: String = checkNotNull(savedStateHandle["signatarioId"])

    private val _uiState = MutableStateFlow(DashboardSignatarioUiState())
    val uiState: StateFlow<DashboardSignatarioUiState> = _uiState.asStateFlow()

    init {
        carregarDadosSignatario()
        observarDocumentos()
    }

    private fun carregarDadosSignatario() {
        viewModelScope.launch {
            val resultado = signatarioRepository.buscarSignatarioPorId(signatarioId)
            if (resultado is ResultadoOperacao.Sucesso) {
                _uiState.update { it.copy(usuarioSignatario = resultado.dados) }
            }
        }
    }

    private fun observarDocumentos() {
        obterDashboardSignatarioUseCase(signatarioId)
            .onEach { lista ->
                _uiState.update {
                    it.copy(
                        todosDocumentos = lista,
                        carregando = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun selecionarAba(status: StatusAssinatura) {
        _uiState.update { it.copy(abaSelecionada = status) }
    }

    fun realizarLogout(aoConcluir: () -> Unit) {
        viewModelScope.launch {
            realizarLogoutUseCase()
            aoConcluir()
        }
    }
}

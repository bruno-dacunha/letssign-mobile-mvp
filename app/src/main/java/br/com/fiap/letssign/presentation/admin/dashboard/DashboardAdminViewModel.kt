package br.com.fiap.letssign.presentation.admin.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.letssign.domain.model.StatusDocumento
import br.com.fiap.letssign.domain.usecase.autenticacao.RealizarLogoutUseCase
import br.com.fiap.letssign.domain.usecase.documento.ObterDashboardAdminUseCase
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
class DashboardAdminViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val obterDashboardAdminUseCase: ObterDashboardAdminUseCase,
    private val realizarLogoutUseCase: RealizarLogoutUseCase
) : ViewModel() {

    val adminId: String = checkNotNull(savedStateHandle["adminId"])

    private val _uiState = MutableStateFlow(DashboardAdminUiState())
    val uiState: StateFlow<DashboardAdminUiState> = _uiState.asStateFlow()

    init {
        observarDocumentos()
    }

    private fun observarDocumentos() {
        obterDashboardAdminUseCase(adminId)
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

    fun selecionarAba(status: StatusDocumento) {
        _uiState.update { it.copy(abaSelecionada = status) }
    }

    fun realizarLogout(aoConcluir: () -> Unit) {
        viewModelScope.launch {
            realizarLogoutUseCase()
            aoConcluir()
        }
    }
}

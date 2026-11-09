package br.com.fiap.letssign.presentation.admin.signatarios

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.model.Usuario
import br.com.fiap.letssign.domain.usecase.signatario.ListarSignatariosDoAdminUseCase
import br.com.fiap.letssign.domain.usecase.signatario.SalvarSignatarioUseCase
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
class GestaoSignatariosViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val listarSignatariosUseCase: ListarSignatariosDoAdminUseCase,
    private val salvarSignatarioUseCase: SalvarSignatarioUseCase
) : ViewModel() {

    private val adminId: String = checkNotNull(savedStateHandle["adminId"])

    private val _uiState = MutableStateFlow(GestaoSignatariosUiState())
    val uiState: StateFlow<GestaoSignatariosUiState> = _uiState.asStateFlow()

    init {
        observarSignatarios()
    }

    private fun observarSignatarios() {
        listarSignatariosUseCase(adminId)
            .onEach { lista ->
                _uiState.update {
                    it.copy(
                        signatarios = lista,
                        carregandoLista = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun aoAlterarTermoBusca(novoTermo: String) {
        _uiState.update { it.copy(termoBusca = novoTermo) }
    }

    fun abrirFormularioNovoSignatario() {
        _uiState.update {
            it.copy(
                exibirFormularioDialog = true,
                signatarioEmEdicao = null,
                nomeCompletoInput = "",
                emailInput = "",
                telefoneInput = "",
                senhaInput = "",
                erroFormulario = null
            )
        }
    }

    fun abrirFormularioEditarSignatario(signatario: Usuario) {
        _uiState.update {
            it.copy(
                exibirFormularioDialog = true,
                signatarioEmEdicao = signatario,
                nomeCompletoInput = signatario.nomeCompleto,
                emailInput = signatario.email,
                telefoneInput = signatario.telefone,
                senhaInput = "",
                erroFormulario = null
            )
        }
    }

    fun fecharFormulario() {
        _uiState.update {
            it.copy(
                exibirFormularioDialog = false,
                signatarioEmEdicao = null,
                erroFormulario = null
            )
        }
    }

    fun aoAlterarNome(valor: String) {
        _uiState.update { it.copy(nomeCompletoInput = valor, erroFormulario = null) }
    }

    fun aoAlterarEmail(valor: String) {
        _uiState.update { it.copy(emailInput = valor, erroFormulario = null) }
    }

    fun aoAlterarTelefone(valor: String) {
        _uiState.update { it.copy(telefoneInput = valor, erroFormulario = null) }
    }

    fun aoAlterarSenha(valor: String) {
        _uiState.update { it.copy(senhaInput = valor, erroFormulario = null) }
    }

    fun salvarSignatario() {
        val estado = _uiState.value
        if (estado.salvando) return

        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erroFormulario = null) }

            val resultado = salvarSignatarioUseCase(
                adminId = adminId,
                signatarioIdEdicao = estado.signatarioEmEdicao?.id,
                nomeCompleto = estado.nomeCompletoInput,
                email = estado.emailInput,
                telefone = estado.telefoneInput,
                senha = estado.senhaInput
            )

            when (resultado) {
                is ResultadoOperacao.Sucesso -> {
                    val acao = if (estado.signatarioEmEdicao == null) "cadastrado" else "atualizado"
                    _uiState.update {
                        it.copy(
                            salvando = false,
                            exibirFormularioDialog = false,
                            signatarioEmEdicao = null,
                            mensagemSucesso = "Signatário '${resultado.dados.nomeCompleto}' $acao com sucesso!"
                        )
                    }
                }
                is ResultadoOperacao.Erro -> {
                    _uiState.update {
                        it.copy(
                            salvando = false,
                            erroFormulario = resultado.mensagem
                        )
                    }
                }
            }
        }
    }

    fun limparMensagemSucesso() {
        _uiState.update { it.copy(mensagemSucesso = null) }
    }
}

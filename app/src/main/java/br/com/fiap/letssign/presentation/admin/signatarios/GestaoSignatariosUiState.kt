package br.com.fiap.letssign.presentation.admin.signatarios

import br.com.fiap.letssign.domain.model.Usuario

data class GestaoSignatariosUiState(
    val signatarios: List<Usuario> = emptyList(),
    val termoBusca: String = "",
    val carregandoLista: Boolean = true,
    val exibirFormularioDialog: Boolean = false,
    val signatarioEmEdicao: Usuario? = null,
    val nomeCompletoInput: String = "",
    val emailInput: String = "",
    val telefoneInput: String = "",
    val senhaInput: String = "",
    val salvando: Boolean = false,
    val erroFormulario: String? = null,
    val mensagemSucesso: String? = null
) {
    val signatariosFiltrados: List<Usuario>
        get() = if (termoBusca.isBlank()) {
            signatarios
        } else {
            val termo = termoBusca.trim().lowercase()
            signatarios.filter {
                it.nomeCompleto.lowercase().contains(termo) ||
                    it.email.lowercase().contains(termo) ||
                    it.telefone.contains(termo)
            }
        }
}

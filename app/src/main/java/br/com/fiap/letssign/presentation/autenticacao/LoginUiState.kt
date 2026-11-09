package br.com.fiap.letssign.presentation.autenticacao

import br.com.fiap.letssign.domain.model.Usuario

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val senhaVisivel: Boolean = false,
    val carregando: Boolean = false,
    val mensagemErro: String? = null,
    val usuarioAutenticado: Usuario? = null
)

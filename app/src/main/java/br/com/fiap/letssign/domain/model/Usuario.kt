package br.com.fiap.letssign.domain.model

data class Usuario(
    val id: String,
    val nomeCompleto: String,
    val email: String,
    val telefone: String,
    val perfil: PerfilUsuario,
    val criadoPorAdminId: String? = null,
    val criadoEm: Long
)

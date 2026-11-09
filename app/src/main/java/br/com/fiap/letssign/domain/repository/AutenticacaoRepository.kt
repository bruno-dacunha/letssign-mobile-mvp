package br.com.fiap.letssign.domain.repository

import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface AutenticacaoRepository {
    suspend fun autenticar(email: String, senha: String): ResultadoOperacao<Usuario>
    fun observarUsuarioLogado(): Flow<Usuario?>
    suspend fun encerrarSessao()
}

package br.com.fiap.letssign.domain.repository

import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface SignatarioRepository {
    fun observarSignatariosDoAdmin(adminId: String): Flow<List<Usuario>>

    suspend fun buscarSignatarioPorId(signatarioId: String): ResultadoOperacao<Usuario>

    suspend fun cadastrarSignatario(
        adminId: String,
        nomeCompleto: String,
        email: String,
        telefone: String,
        senha: String
    ): ResultadoOperacao<Usuario>

    suspend fun atualizarSignatario(
        signatarioId: String,
        nomeCompleto: String,
        email: String,
        telefone: String,
        novaSenhaOpcional: String?
    ): ResultadoOperacao<Usuario>
}

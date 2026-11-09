package br.com.fiap.letssign.data.repository

import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.data.local.dao.UsuarioDao
import br.com.fiap.letssign.data.local.db.SeedInicialBanco
import br.com.fiap.letssign.data.local.entity.UsuarioEntity
import br.com.fiap.letssign.data.mapper.paraDominio
import br.com.fiap.letssign.domain.model.PerfilUsuario
import br.com.fiap.letssign.domain.model.Usuario
import br.com.fiap.letssign.domain.repository.SignatarioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignatarioRepositoryImpl @Inject constructor(
    private val usuarioDao: UsuarioDao
) : SignatarioRepository {

    override fun observarSignatariosDoAdmin(adminId: String): Flow<List<Usuario>> {
        return usuarioDao.observarSignatariosDoAdmin(adminId)
            .onStart {
                if (usuarioDao.contarUsuarios() == 0) {
                    usuarioDao.inserirIgnorandoConflito(SeedInicialBanco.obterUsuariosIniciais())
                }
            }
            .map { lista -> lista.map { it.paraDominio() } }
    }

    override suspend fun buscarSignatarioPorId(signatarioId: String): ResultadoOperacao<Usuario> {
        return try {
            val entity = usuarioDao.buscarPorId(signatarioId)
                ?: return ResultadoOperacao.Erro("Signatário não encontrado.")
            ResultadoOperacao.Sucesso(entity.paraDominio())
        } catch (e: Exception) {
            ResultadoOperacao.Erro("Erro ao buscar signatário: ${e.localizedMessage}", e)
        }
    }

    override suspend fun cadastrarSignatario(
        adminId: String,
        nomeCompleto: String,
        email: String,
        telefone: String,
        senha: String
    ): ResultadoOperacao<Usuario> {
        return try {
            val emailNormalizado = email.trim().lowercase()
            val existente = usuarioDao.buscarPorEmail(emailNormalizado)
            if (existente != null) {
                return ResultadoOperacao.Erro("Já existe um usuário cadastrado com este e-mail.")
            }

            val novoEntity = UsuarioEntity(
                id = UUID.randomUUID().toString(),
                nomeCompleto = nomeCompleto.trim(),
                email = emailNormalizado,
                telefone = telefone.trim(),
                senhaHash = senha,
                perfil = PerfilUsuario.SIGNATARIO.name,
                criadoPorAdminId = adminId,
                criadoEm = System.currentTimeMillis()
            )
            usuarioDao.inserir(novoEntity)
            ResultadoOperacao.Sucesso(novoEntity.paraDominio())
        } catch (e: Exception) {
            ResultadoOperacao.Erro("Falha ao cadastrar signatário: ${e.localizedMessage}", e)
        }
    }

    override suspend fun atualizarSignatario(
        signatarioId: String,
        nomeCompleto: String,
        email: String,
        telefone: String,
        novaSenhaOpcional: String?
    ): ResultadoOperacao<Usuario> {
        return try {
            val atual = usuarioDao.buscarPorId(signatarioId)
                ?: return ResultadoOperacao.Erro("Signatário não encontrado para atualização.")

            val emailNormalizado = email.trim().lowercase()
            val conflitoEmail = usuarioDao.buscarPorEmail(emailNormalizado)
            if (conflitoEmail != null && conflitoEmail.id != signatarioId) {
                return ResultadoOperacao.Erro("Este e-mail já está em uso por outro usuário.")
            }

            val atualizado = atual.copy(
                nomeCompleto = nomeCompleto.trim(),
                email = emailNormalizado,
                telefone = telefone.trim(),
                senhaHash = novaSenhaOpcional?.takeIf { it.isNotBlank() } ?: atual.senhaHash
            )
            usuarioDao.atualizar(atualizado)
            ResultadoOperacao.Sucesso(atualizado.paraDominio())
        } catch (e: Exception) {
            ResultadoOperacao.Erro("Falha ao atualizar signatário: ${e.localizedMessage}", e)
        }
    }
}

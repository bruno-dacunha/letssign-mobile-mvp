package br.com.fiap.letssign.data.repository

import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.data.local.dao.UsuarioDao
import br.com.fiap.letssign.data.local.db.SeedInicialBanco
import br.com.fiap.letssign.data.local.preferencias.SessaoUsuarioPreferences
import br.com.fiap.letssign.data.mapper.paraDominio
import br.com.fiap.letssign.domain.model.Usuario
import br.com.fiap.letssign.domain.repository.AutenticacaoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutenticacaoRepositoryImpl @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val sessaoPreferences: SessaoUsuarioPreferences
) : AutenticacaoRepository {

    private suspend fun garantirSeedInicial() {
        if (usuarioDao.contarUsuarios() == 0) {
            usuarioDao.inserirIgnorandoConflito(SeedInicialBanco.obterUsuariosIniciais())
        }
    }

    override suspend fun autenticar(email: String, senha: String): ResultadoOperacao<Usuario> {
        return try {
            garantirSeedInicial()

            val usuarioEntity = usuarioDao.buscarPorEmail(email.trim().lowercase())
                ?: return ResultadoOperacao.Erro("Nenhuma conta encontrada para este e-mail.")

            if (usuarioEntity.senhaHash != senha) {
                return ResultadoOperacao.Erro("Senha incorreta. Verifique suas credenciais e tente novamente.")
            }

            sessaoPreferences.salvarSessaoUsuario(usuarioEntity.id)
            ResultadoOperacao.Sucesso(usuarioEntity.paraDominio())
        } catch (e: Exception) {
            ResultadoOperacao.Erro(
                mensagem = "Falha ao autenticar no banco local: ${e.localizedMessage}",
                causa = e
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observarUsuarioLogado(): Flow<Usuario?> {
        return sessaoPreferences.usuarioLogadoIdFlow.flatMapLatest { usuarioId ->
            if (usuarioId.isNullOrBlank()) {
                flowOf(null)
            } else {
                usuarioDao.observarPorId(usuarioId).map { it?.paraDominio() }
            }
        }
    }

    override suspend fun encerrarSessao() {
        sessaoPreferences.limparSessao()
    }
}

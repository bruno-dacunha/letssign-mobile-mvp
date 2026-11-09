package br.com.fiap.letssign.domain.usecase.autenticacao

import br.com.fiap.letssign.domain.model.Usuario
import br.com.fiap.letssign.domain.repository.AutenticacaoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObterUsuarioLogadoUseCase @Inject constructor(
    private val autenticacaoRepository: AutenticacaoRepository
) {
    operator fun invoke(): Flow<Usuario?> = autenticacaoRepository.observarUsuarioLogado()
}

class RealizarLogoutUseCase @Inject constructor(
    private val autenticacaoRepository: AutenticacaoRepository
) {
    suspend operator fun invoke() = autenticacaoRepository.encerrarSessao()
}

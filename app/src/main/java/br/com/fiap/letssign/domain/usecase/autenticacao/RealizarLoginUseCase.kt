package br.com.fiap.letssign.domain.usecase.autenticacao

import android.util.Patterns
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.model.Usuario
import br.com.fiap.letssign.domain.repository.AutenticacaoRepository
import javax.inject.Inject

class RealizarLoginUseCase @Inject constructor(
    private val autenticacaoRepository: AutenticacaoRepository
) {
    suspend operator fun invoke(email: String, senha: String): ResultadoOperacao<Usuario> {
        val emailLimpo = email.trim().lowercase()

        if (emailLimpo.isBlank()) {
            return ResultadoOperacao.Erro("Informe o seu e-mail para acessar.")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailLimpo).matches()) {
            return ResultadoOperacao.Erro("Formato de e-mail inválido.")
        }
        if (senha.isBlank()) {
            return ResultadoOperacao.Erro("Informe a sua senha.")
        }
        if (senha.length < 4) {
            return ResultadoOperacao.Erro("A senha deve possuir ao menos 4 caracteres.")
        }

        return autenticacaoRepository.autenticar(email = emailLimpo, senha = senha)
    }
}

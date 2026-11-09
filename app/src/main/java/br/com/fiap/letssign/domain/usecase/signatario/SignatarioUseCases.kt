package br.com.fiap.letssign.domain.usecase.signatario

import android.util.Patterns
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.model.Usuario
import br.com.fiap.letssign.domain.repository.SignatarioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ListarSignatariosDoAdminUseCase @Inject constructor(
    private val signatarioRepository: SignatarioRepository
) {
    operator fun invoke(adminId: String): Flow<List<Usuario>> =
        signatarioRepository.observarSignatariosDoAdmin(adminId)
}

class SalvarSignatarioUseCase @Inject constructor(
    private val signatarioRepository: SignatarioRepository
) {
    suspend operator fun invoke(
        adminId: String,
        signatarioIdEdicao: String?,
        nomeCompleto: String,
        email: String,
        telefone: String,
        senha: String
    ): ResultadoOperacao<Usuario> {
        val nomeLimpo = nomeCompleto.trim()
        val emailLimpo = email.trim().lowercase()
        val telefoneLimpo = telefone.trim()
        val apenasDigitosTelefone = telefoneLimpo.filter { it.isDigit() }

        if (nomeLimpo.length < 3 || !nomeLimpo.contains(" ")) {
            return ResultadoOperacao.Erro("Informe o nome e sobrenome completos do signatário.")
        }
        if (emailLimpo.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(emailLimpo).matches()) {
            return ResultadoOperacao.Erro("Informe um endereço de e-mail válido.")
        }
        if (apenasDigitosTelefone.length < 8) {
            return ResultadoOperacao.Erro("Informe um telefone válido com DDD (mínimo de 8 dígitos).")
        }

        return if (signatarioIdEdicao == null) {
            if (senha.length < 4) {
                return ResultadoOperacao.Erro("Defina uma senha inicial com ao menos 4 caracteres.")
            }
            signatarioRepository.cadastrarSignatario(
                adminId = adminId,
                nomeCompleto = nomeLimpo,
                email = emailLimpo,
                telefone = telefoneLimpo,
                senha = senha
            )
        } else {
            if (senha.isNotBlank() && senha.length < 4) {
                return ResultadoOperacao.Erro("Para alterar a senha, informe ao menos 4 caracteres.")
            }
            signatarioRepository.atualizarSignatario(
                signatarioId = signatarioIdEdicao,
                nomeCompleto = nomeLimpo,
                email = emailLimpo,
                telefone = telefoneLimpo,
                novaSenhaOpcional = senha.takeIf { it.isNotBlank() }
            )
        }
    }
}

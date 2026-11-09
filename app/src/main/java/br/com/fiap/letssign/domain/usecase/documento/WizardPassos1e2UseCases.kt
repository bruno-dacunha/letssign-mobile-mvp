package br.com.fiap.letssign.domain.usecase.documento

import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.model.ArquivoPdfValidado
import br.com.fiap.letssign.domain.repository.ArquivoPdfRepository
import br.com.fiap.letssign.domain.repository.SelecaoSignatarioInput
import javax.inject.Inject

class ImportarEValidarPdfUseCase @Inject constructor(
    private val arquivoPdfRepository: ArquivoPdfRepository
) {
    suspend operator fun invoke(uriString: String): ResultadoOperacao<ArquivoPdfValidado> {
        if (uriString.isBlank()) {
            return ResultadoOperacao.Erro("Nenhum arquivo foi selecionado.")
        }
        return arquivoPdfRepository.importarEValidarPdf(uriString)
    }

    suspend fun gerarContratoDemonstracao(titulo: String): ResultadoOperacao<ArquivoPdfValidado> {
        val tituloFinal = titulo.trim().ifBlank { "Contrato de Prestação de Serviços LetsSign" }
        return arquivoPdfRepository.gerarPdfContratoDemonstracao(tituloFinal)
    }
}

class ValidarPasso1UploadUseCase @Inject constructor() {
    operator fun invoke(
        tituloDocumento: String,
        arquivoPdf: ArquivoPdfValidado?
    ): ResultadoOperacao<Unit> {
        val tituloLimpo = tituloDocumento.trim()

        if (tituloLimpo.isBlank()) {
            return ResultadoOperacao.Erro("Informe o título do documento antes de avançar.")
        }
        if (tituloLimpo.length < 3) {
            return ResultadoOperacao.Erro("O título do documento deve ter ao menos 3 caracteres.")
        }
        if (arquivoPdf == null) {
            return ResultadoOperacao.Erro("Selecione um arquivo PDF válido no seu celular para continuar.")
        }
        if (arquivoPdf.totalPaginas <= 0) {
            return ResultadoOperacao.Erro("O arquivo PDF selecionado não possui páginas válidas.")
        }

        return ResultadoOperacao.Sucesso(Unit)
    }
}

class ValidarPasso2SignatariosUseCase @Inject constructor() {
    operator fun invoke(
        signatariosSelecionados: List<SelecaoSignatarioInput>
    ): ResultadoOperacao<Unit> {
        if (signatariosSelecionados.isEmpty()) {
            return ResultadoOperacao.Erro(
                "Selecione pelo menos 1 signatário para assinar este documento."
            )
        }

        val possuiAoMenosUmObrigatorio = signatariosSelecionados.any { it.obrigatorio }
        if (!possuiAoMenosUmObrigatorio) {
            return ResultadoOperacao.Erro(
                "O documento deve possuir no mínimo 1 signatário marcado como obrigatório."
            )
        }

        return ResultadoOperacao.Sucesso(Unit)
    }
}

package br.com.fiap.letssign.domain.usecase.documento

import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.model.ArquivoPdfValidado
import br.com.fiap.letssign.domain.model.DetalhesAcompanhamentoDocumento
import br.com.fiap.letssign.domain.model.Documento
import br.com.fiap.letssign.domain.repository.DocumentoRepository
import br.com.fiap.letssign.domain.repository.PosicaoCampoInput
import br.com.fiap.letssign.domain.repository.SelecaoSignatarioInput
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConfirmarEnvioDocumentoUseCase @Inject constructor(
    private val validarPasso1UploadUseCase: ValidarPasso1UploadUseCase,
    private val validarPasso2SignatariosUseCase: ValidarPasso2SignatariosUseCase,
    private val validarPasso3PosicionamentoUseCase: ValidarPasso3PosicionamentoUseCase,
    private val documentoRepository: DocumentoRepository
) {
    suspend operator fun invoke(
        adminId: String,
        tituloDocumento: String,
        arquivoPdf: ArquivoPdfValidado?,
        mensagemConvite: String,
        signatariosSelecionados: List<SelecaoSignatarioInput>,
        camposPosicionados: List<PosicaoCampoInput>,
        nomesPorSignatarioId: Map<String, String>,
        documentoIdTentativa: String? = null
    ): ResultadoOperacao<Documento> {
        val validacaoPasso1 = validarPasso1UploadUseCase(tituloDocumento, arquivoPdf)
        if (validacaoPasso1 is ResultadoOperacao.Erro) return validacaoPasso1

        val validacaoPasso2 = validarPasso2SignatariosUseCase(signatariosSelecionados)
        if (validacaoPasso2 is ResultadoOperacao.Erro) return validacaoPasso2

        val pdfValidado = checkNotNull(arquivoPdf)
        val validacaoPasso3 = validarPasso3PosicionamentoUseCase(
            totalPaginasPdf = pdfValidado.totalPaginas,
            signatariosSelecionados = signatariosSelecionados,
            camposPosicionados = camposPosicionados,
            nomesPorSignatarioId = nomesPorSignatarioId
        )
        if (validacaoPasso3 is ResultadoOperacao.Erro) return validacaoPasso3

        val mensagemFinal = mensagemConvite.trim().ifBlank {
            "Olá! Você recebeu o documento '${tituloDocumento.trim()}' para assinatura digital via LetsSign."
        }

        return documentoRepository.criarRascunhoEEnviarDocumento(
            adminId = adminId,
            titulo = tituloDocumento.trim(),
            caminhoPdfImportado = pdfValidado.caminhoLocalPrivado,
            totalPaginas = pdfValidado.totalPaginas,
            mensagemConvite = mensagemFinal,
            signatariosSelecionados = signatariosSelecionados,
            camposPosicionados = camposPosicionados,
            documentoIdExistenteParaReenvio = documentoIdTentativa
        )
    }
}

class ObterDetalhesAcompanhamentoUseCase @Inject constructor(
    private val documentoRepository: DocumentoRepository
) {
    operator fun invoke(documentoId: String): Flow<DetalhesAcompanhamentoDocumento?> =
        documentoRepository.observarDetalhesAcompanhamento(documentoId)
}

package br.com.fiap.letssign.domain.usecase.documento

import android.graphics.Bitmap
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.model.CampoAssinatura
import br.com.fiap.letssign.domain.model.Documento
import br.com.fiap.letssign.domain.repository.ArquivoPdfRepository
import br.com.fiap.letssign.domain.repository.DocumentoRepository
import javax.inject.Inject

class AssinarDocumentoUseCase @Inject constructor(
    private val arquivoPdfRepository: ArquivoPdfRepository,
    private val documentoRepository: DocumentoRepository
) {
    suspend operator fun invoke(
        documentoId: String,
        signatarioId: String,
        nomeSignatario: String,
        papelSignatario: String,
        caminhoPdfAtual: String,
        camposDoSignatario: List<CampoAssinatura>,
        bitmapAssinatura: Bitmap
    ): ResultadoOperacao<Documento> {
        if (camposDoSignatario.isEmpty()) {
            return ResultadoOperacao.Erro(
                "Não foram encontrados campos posicionados para a sua assinatura neste documento."
            )
        }

        val resultadoEstampa = arquivoPdfRepository.estamparAssinaturaManuscritaNoPdf(
            caminhoPdfEntrada = caminhoPdfAtual,
            documentoId = documentoId,
            nomeSignatario = nomeSignatario,
            papelSignatario = papelSignatario,
            camposDoSignatario = camposDoSignatario,
            bitmapAssinatura = bitmapAssinatura
        )

        val dadosEstampa = when (resultadoEstampa) {
            is ResultadoOperacao.Sucesso -> resultadoEstampa.dados
            is ResultadoOperacao.Erro -> return resultadoEstampa
        }

        return documentoRepository.registrarAssinaturaDoSignatario(
            documentoId = documentoId,
            signatarioId = signatarioId,
            caminhoPdfAssinado = dadosEstampa.caminhoPdfAssinado,
            caminhoImagemRubrica = dadosEstampa.caminhoImagemRubrica
        )
    }
}

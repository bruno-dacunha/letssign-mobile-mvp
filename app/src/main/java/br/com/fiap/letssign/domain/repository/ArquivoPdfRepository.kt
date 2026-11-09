package br.com.fiap.letssign.domain.repository

import android.graphics.Bitmap
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.core.pdf.ResultadoEstampaPdf
import br.com.fiap.letssign.domain.model.ArquivoPdfValidado
import br.com.fiap.letssign.domain.model.CampoAssinatura

interface ArquivoPdfRepository {

    suspend fun importarEValidarPdf(uriString: String): ResultadoOperacao<ArquivoPdfValidado>

    suspend fun gerarPdfContratoDemonstracao(titulo: String): ResultadoOperacao<ArquivoPdfValidado>

    suspend fun estamparAssinaturaManuscritaNoPdf(
        caminhoPdfEntrada: String,
        documentoId: String,
        nomeSignatario: String,
        papelSignatario: String,
        camposDoSignatario: List<CampoAssinatura>,
        bitmapAssinatura: Bitmap
    ): ResultadoOperacao<ResultadoEstampaPdf>
}

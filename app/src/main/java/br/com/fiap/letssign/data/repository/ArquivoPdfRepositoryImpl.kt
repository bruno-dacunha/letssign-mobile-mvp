package br.com.fiap.letssign.data.repository

import android.graphics.Bitmap
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.core.pdf.GravadorAssinaturaPdf
import br.com.fiap.letssign.core.pdf.ResultadoEstampaPdf
import br.com.fiap.letssign.data.local.arquivos.ArmazenamentoPdfLocalDataSource
import br.com.fiap.letssign.domain.model.ArquivoPdfValidado
import br.com.fiap.letssign.domain.model.CampoAssinatura
import br.com.fiap.letssign.domain.repository.ArquivoPdfRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArquivoPdfRepositoryImpl @Inject constructor(
    private val localDataSource: ArmazenamentoPdfLocalDataSource,
    private val gravadorAssinaturaPdf: GravadorAssinaturaPdf
) : ArquivoPdfRepository {

    override suspend fun importarEValidarPdf(uriString: String): ResultadoOperacao<ArquivoPdfValidado> {
        return localDataSource.importarEValidarPdfDeUri(uriString)
    }

    override suspend fun gerarPdfContratoDemonstracao(titulo: String): ResultadoOperacao<ArquivoPdfValidado> {
        return localDataSource.gerarPdfDemonstracaoLocal(titulo)
    }

    override suspend fun estamparAssinaturaManuscritaNoPdf(
        caminhoPdfEntrada: String,
        documentoId: String,
        nomeSignatario: String,
        papelSignatario: String,
        camposDoSignatario: List<CampoAssinatura>,
        bitmapAssinatura: Bitmap
    ): ResultadoOperacao<ResultadoEstampaPdf> {
        return gravadorAssinaturaPdf.estamparAssinaturaNoPdf(
            caminhoPdfEntrada = caminhoPdfEntrada,
            documentoId = documentoId,
            nomeSignatario = nomeSignatario,
            papelSignatario = papelSignatario,
            camposDoSignatario = camposDoSignatario,
            bitmapAssinatura = bitmapAssinatura
        )
    }
}

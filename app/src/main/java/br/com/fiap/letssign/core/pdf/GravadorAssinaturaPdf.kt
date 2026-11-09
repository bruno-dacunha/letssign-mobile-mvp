package br.com.fiap.letssign.core.pdf

import android.content.Context
import android.graphics.Bitmap
import br.com.fiap.letssign.core.comum.FormatadorData
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.model.CampoAssinatura
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class ResultadoEstampaPdf(
    val caminhoPdfAssinado: String,
    val caminhoImagemRubrica: String
)

@Singleton
class GravadorAssinaturaPdf @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pastaDocumentos: File
        get() = File(context.filesDir, "documentos").apply { if (!exists()) mkdirs() }

    private val pastaAssinaturas: File
        get() = File(context.filesDir, "assinaturas").apply { if (!exists()) mkdirs() }

    suspend fun estamparAssinaturaNoPdf(
        caminhoPdfEntrada: String,
        documentoId: String,
        nomeSignatario: String,
        papelSignatario: String,
        camposDoSignatario: List<CampoAssinatura>,
        bitmapAssinatura: Bitmap
    ): ResultadoOperacao<ResultadoEstampaPdf> = withContext(Dispatchers.IO) {
        try {
            val arquivoEntrada = File(caminhoPdfEntrada)
            if (!arquivoEntrada.exists()) {
                return@withContext ResultadoOperacao.Erro(
                    "O arquivo PDF original não foi encontrado no dispositivo."
                )
            }
            if (camposDoSignatario.isEmpty()) {
                return@withContext ResultadoOperacao.Erro(
                    "Nenhum campo de assinatura demarcado para este signatário."
                )
            }

            val agora = System.currentTimeMillis()
            val idAssinatura = UUID.randomUUID().toString()

            val arquivoRubricaPng = File(pastaAssinaturas, "rubrica_${idAssinatura}.png")
            FileOutputStream(arquivoRubricaPng).use { out ->
                bitmapAssinatura.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val arquivoPdfSaida = File(
                pastaDocumentos,
                "pdf_assinado_${documentoId.take(8)}_${agora}.pdf"
            )

            PDDocument.load(arquivoEntrada).use { pdDocument ->
                val imagemPdfBox = LosslessFactory.createFromImage(pdDocument, bitmapAssinatura)
                val totalPaginas = pdDocument.numberOfPages

                for (campo in camposDoSignatario) {
                    val indicePagina = campo.pagina.coerceIn(0, totalPaginas - 1)
                    val paginaPdf = pdDocument.getPage(indicePagina)
                    val cropBox = paginaPdf.cropBox

                    val larguraPaginaPts = cropBox.width
                    val alturaPaginaPts = cropBox.height

                    // Na UI a origem (0,0) é o topo-esquerdo; no PDFBox é a base-esquerda
                    val larguraCampoPts = (campo.larguraRelativa * larguraPaginaPts)
                        .coerceAtLeast(60f)
                    val alturaCampoPts = (campo.alturaRelativa * alturaPaginaPts)
                        .coerceAtLeast(28f)

                    val xPdfPts = cropBox.lowerLeftX + (campo.posicaoXRelativa * larguraPaginaPts)
                    val yPdfPts = cropBox.lowerLeftY +
                        alturaPaginaPts -
                        (campo.posicaoYRelativa * alturaPaginaPts) -
                        alturaCampoPts

                    PDPageContentStream(
                        pdDocument,
                        paginaPdf,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true
                    ).use { contentStream ->
                        val alturaRodapeSelo = 10f
                        val alturaImagemPts = (alturaCampoPts - alturaRodapeSelo).coerceAtLeast(16f)
                        val yImagemPts = yPdfPts + alturaRodapeSelo

                        contentStream.drawImage(
                            imagemPdfBox,
                            xPdfPts,
                            yImagemPts,
                            larguraCampoPts,
                            alturaImagemPts
                        )

                        contentStream.setStrokingColor(67, 24, 255)
                        contentStream.setLineWidth(0.6f)
                        contentStream.moveTo(xPdfPts, yImagemPts - 1f)
                        contentStream.lineTo(xPdfPts + larguraCampoPts, yImagemPts - 1f)
                        contentStream.stroke()

                        val textoSelo = removerAcentos(
                            "Assinado por ${nomeSignatario.take(22)} ($papelSignatario) - ${FormatadorData.formatarDataHora(agora)}"
                        )
                        contentStream.beginText()
                        contentStream.setNonStrokingColor(27, 37, 89)
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 6.2f)
                        contentStream.newLineAtOffset(xPdfPts + 2f, yPdfPts + 2f)
                        contentStream.showText(textoSelo)
                        contentStream.endText()
                    }
                }

                pdDocument.save(arquivoPdfSaida)
            }

            ResultadoOperacao.Sucesso(
                ResultadoEstampaPdf(
                    caminhoPdfAssinado = arquivoPdfSaida.absolutePath,
                    caminhoImagemRubrica = arquivoRubricaPng.absolutePath
                )
            )
        } catch (e: Exception) {
            ResultadoOperacao.Erro(
                "Erro ao estampar assinatura no PDF: ${e.localizedMessage}",
                e
            )
        }
    }

    private fun removerAcentos(texto: String): String {
        val normalizado = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
        return normalizado.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }
}

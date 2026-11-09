package br.com.fiap.letssign.data.local.arquivos

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.model.ArquivoPdfValidado
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArmazenamentoPdfLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pastaDocumentos: File
        get() = File(context.filesDir, "documentos").apply {
            if (!exists()) mkdirs()
        }

    suspend fun importarEValidarPdfDeUri(uriString: String): ResultadoOperacao<ArquivoPdfValidado> =
        withContext(Dispatchers.IO) {
            var arquivoDestino: File? = null
            try {
                val uri = Uri.parse(uriString)
                val contentResolver = context.contentResolver

                val nomeOriginal = obterNomeArquivo(uri) ?: "documento_importado.pdf"
                val idUnico = UUID.randomUUID().toString()
                arquivoDestino = File(pastaDocumentos, "pdf_${idUnico}.pdf")

                val inputStream = contentResolver.openInputStream(uri)
                    ?: return@withContext ResultadoOperacao.Erro(
                        "Não foi possível abrir o arquivo selecionado no dispositivo."
                    )

                inputStream.use { entrada ->
                    FileOutputStream(arquivoDestino).use { saida ->
                        entrada.copyTo(saida)
                    }
                }

                if (!arquivoDestino.exists() || arquivoDestino.length() < 8L) {
                    arquivoDestino.delete()
                    return@withContext ResultadoOperacao.Erro(
                        "O arquivo selecionado está vazio ou inacessível."
                    )
                }

                val cabecalhoValido = FileInputStream(arquivoDestino).use { fis ->
                    val buffer = ByteArray(5)
                    val lidos = fis.read(buffer)
                    lidos == 5 && String(buffer, Charsets.US_ASCII) == "%PDF-"
                }

                if (!cabecalhoValido) {
                    arquivoDestino.delete()
                    return@withContext ResultadoOperacao.Erro(
                        "Arquivo inválido: o documento selecionado não possui estrutura de um arquivo PDF."
                    )
                }

                val totalPaginasPdfBox = PDDocument.load(arquivoDestino).use { pdDoc ->
                    if (pdDoc.isEncrypted) {
                        arquivoDestino.delete()
                        return@withContext ResultadoOperacao.Erro(
                            "Este PDF está protegido por senha e não pode ser processado para assinatura."
                        )
                    }
                    pdDoc.numberOfPages
                }

                if (totalPaginasPdfBox <= 0) {
                    arquivoDestino.delete()
                    return@withContext ResultadoOperacao.Erro(
                        "O arquivo PDF não contém páginas válidas."
                    )
                }

                ParcelFileDescriptor.open(arquivoDestino, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        if (renderer.pageCount <= 0) {
                            throw IllegalStateException("PDF sem páginas renderizáveis.")
                        }
                        renderer.openPage(0).use { }
                    }
                }

                ResultadoOperacao.Sucesso(
                    ArquivoPdfValidado(
                        caminhoLocalPrivado = arquivoDestino.absolutePath,
                        nomeArquivoOriginal = nomeOriginal,
                        tamanhoBytes = arquivoDestino.length(),
                        totalPaginas = totalPaginasPdfBox
                    )
                )
            } catch (e: SecurityException) {
                arquivoDestino?.delete()
                ResultadoOperacao.Erro(
                    "O arquivo PDF está protegido ou sem permissão de leitura.",
                    e
                )
            } catch (e: Exception) {
                arquivoDestino?.delete()
                ResultadoOperacao.Erro(
                    "O arquivo PDF selecionado está corrompido ou não pôde ser processado.",
                    e
                )
            }
        }

    suspend fun gerarPdfDemonstracaoLocal(titulo: String): ResultadoOperacao<ArquivoPdfValidado> =
        withContext(Dispatchers.IO) {
            try {
                val idUnico = UUID.randomUUID().toString()
                val arquivoPdf = File(pastaDocumentos, "contrato_letssign_${idUnico}.pdf")

                PDDocument().use { doc ->
                    val pagina1 = PDPage(PDRectangle.A4)
                    doc.addPage(pagina1)
                    PDPageContentStream(doc, pagina1).use { cs ->
                        cs.beginText()
                        cs.setFont(PDType1Font.HELVETICA_BOLD, 18f)
                        cs.newLineAtOffset(50f, 770f)
                        cs.showText("LETSSIGN - INSTRUMENTO PARTICULAR DE CONTRATO")
                        cs.endText()

                        cs.beginText()
                        cs.setFont(PDType1Font.HELVETICA_BOLD, 13f)
                        cs.newLineAtOffset(50f, 735f)
                        cs.showText("Documento: ${titulo.take(55)}")
                        cs.endText()

                        val paragrafosPag1 = listOf(
                            "Pelo presente instrumento particular, as partes qualificadas e identificadas",
                            "eletronicamente nesta plataforma celebram o presente Contrato Digital sob",
                            "a egide da Medida Provisoria n. 2.200-2/2001 e da Lei n. 14.063/2020.",
                            "",
                            "CLAUSULA PRIMEIRA - DO OBJETO",
                            "1.1. O presente documento tem por objeto formalizar a contratacao e coleta",
                            "de assinaturas digitais com validade juridica por meio do aplicativo LetsSign.",
                            "",
                            "CLAUSULA SEGUNDA - DA INTEGRIDADE E VALIDADE JURIDICA",
                            "2.1. As partes reconhecem a assinatura eletronica posicionada neste PDF",
                            "como plenamente valida, integra e irretratavel.",
                            "",
                            "[Pagina 1 de 2 - Posicione rubricas ou assinaturas nos locais desejados]"
                        )

                        var yAtual = 690f
                        for (linha in paragrafosPag1) {
                            cs.beginText()
                            cs.setFont(PDType1Font.HELVETICA, 11f)
                            cs.newLineAtOffset(50f, yAtual)
                            cs.showText(linha)
                            cs.endText()
                            yAtual -= 22f
                        }
                    }

                    val pagina2 = PDPage(PDRectangle.A4)
                    doc.addPage(pagina2)
                    PDPageContentStream(doc, pagina2).use { cs ->
                        cs.beginText()
                        cs.setFont(PDType1Font.HELVETICA_BOLD, 16f)
                        cs.newLineAtOffset(50f, 770f)
                        cs.showText("TERMO DE ENCERRAMENTO E ASSINATURAS (PAGINA 2/2)")
                        cs.endText()

                        val paragrafosPag2 = listOf(
                            "CLAUSULA TERCEIRA - DO FORO E DISPOSICOES FINAIS",
                            "3.1. E por estarem assim justas e contratadas, as partes assinam",
                            "digitalmente este instrumento nos campos demarcados pelo Administrador",
                            "(Contratante, Contratado e Testemunha).",
                            "",
                            "Area reservada abaixo para posicionamento dos campos de assinatura (X, Y):"
                        )

                        var yAtual = 720f
                        for (linha in paragrafosPag2) {
                            cs.beginText()
                            cs.setFont(PDType1Font.HELVETICA, 11f)
                            cs.newLineAtOffset(50f, yAtual)
                            cs.showText(linha)
                            cs.endText()
                            yAtual -= 22f
                        }
                    }

                    doc.save(arquivoPdf)
                }

                ResultadoOperacao.Sucesso(
                    ArquivoPdfValidado(
                        caminhoLocalPrivado = arquivoPdf.absolutePath,
                        nomeArquivoOriginal = "Contrato_Modelo_LetsSign.pdf",
                        tamanhoBytes = arquivoPdf.length(),
                        totalPaginas = 2
                    )
                )
            } catch (e: Exception) {
                ResultadoOperacao.Erro(
                    "Falha ao gerar PDF de demonstração: ${e.localizedMessage}",
                    e
                )
            }
        }

    private fun obterNomeArquivo(uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx != -1 && cursor.moveToFirst()) {
                    return cursor.getString(idx)
                }
            }
        }
        return uri.lastPathSegment
    }
}

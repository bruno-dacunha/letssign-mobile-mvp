package br.com.fiap.letssign.core.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class PaginaPdfRenderizada(
    val indicePagina: Int,
    val totalPaginas: Int,
    val bitmap: Bitmap,
    val larguraOriginalPts: Int,
    val alturaOriginalPts: Int
) {
    val proporcaoLarguraAltura: Float
        get() = if (alturaOriginalPts > 0) {
            larguraOriginalPts.toFloat() / alturaOriginalPts.toFloat()
        } else {
            1f / 1.414f
        }
}

@Singleton
class RenderizadorPdfHelper @Inject constructor() {

    suspend fun renderizarPagina(
        caminhoArquivoPdf: String,
        indicePagina: Int,
        escalaQualidade: Int = 2
    ): Result<PaginaPdfRenderizada> = withContext(Dispatchers.IO) {
        runCatching {
            val arquivo = File(caminhoArquivoPdf)
            require(arquivo.exists()) { "Arquivo PDF não encontrado no armazenamento local." }

            ParcelFileDescriptor.open(arquivo, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val total = renderer.pageCount
                    require(total > 0) { "O PDF não possui páginas." }

                    val indiceSeguro = indicePagina.coerceIn(0, total - 1)
                    renderer.openPage(indiceSeguro).use { pagina ->
                        val larguraPts = pagina.width
                        val alturaPts = pagina.height

                        val larguraBitmap = (larguraPts * escalaQualidade).coerceAtLeast(1)
                        val alturaBitmap = (alturaPts * escalaQualidade).coerceAtLeast(1)

                        val bitmap = Bitmap.createBitmap(
                            larguraBitmap,
                            alturaBitmap,
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)

                        pagina.render(
                            bitmap,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )

                        PaginaPdfRenderizada(
                            indicePagina = indiceSeguro,
                            totalPaginas = total,
                            bitmap = bitmap,
                            larguraOriginalPts = larguraPts,
                            alturaOriginalPts = alturaPts
                        )
                    }
                }
            }
        }
    }
}

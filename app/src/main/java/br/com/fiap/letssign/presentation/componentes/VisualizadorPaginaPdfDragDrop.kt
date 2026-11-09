package br.com.fiap.letssign.presentation.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.letssign.core.pdf.PaginaPdfRenderizada
import br.com.fiap.letssign.core.ui.theme.FiapPinkAccent
import br.com.fiap.letssign.core.ui.theme.LetsSignPurplePrimary
import br.com.fiap.letssign.core.ui.theme.StatusSentBlue
import br.com.fiap.letssign.core.ui.theme.StatusSuccessGreen
import br.com.fiap.letssign.core.ui.theme.StatusWarningAmber
import br.com.fiap.letssign.presentation.admin.criacao.CampoAssinaturaPosicionadoUi
import java.util.Locale
import kotlin.math.roundToInt

val CoresSignatariosPdf = listOf(
    LetsSignPurplePrimary,
    FiapPinkAccent,
    StatusSentBlue,
    StatusSuccessGreen,
    StatusWarningAmber
)

fun obterCorSignatario(indiceCor: Int): Color =
    CoresSignatariosPdf[indiceCor.coerceAtLeast(0) % CoresSignatariosPdf.size]

// Renderiza a página do PDF restrita ao espaço disponível e sobrepõe os campos com coordenadas normalizadas (0.0f a 1.0f)
@Composable
fun VisualizadorPaginaPdfDragDrop(
    paginaRenderizada: PaginaPdfRenderizada,
    camposDaPagina: List<CampoAssinaturaPosicionadoUi>,
    campoSelecionadoId: String?,
    aoSelecionarCampo: (String) -> Unit,
    aoAtualizarPosicaoNormalizada: (campoId: String, novoXRelativo: Float, novoYRelativo: Float) -> Unit,
    aoAlternarDimensaoCampo: (campoId: String) -> Unit,
    aoExcluirCampo: (campoId: String) -> Unit,
    aoTocarNaPaginaParaInserir: (xRelativoToque: Float, yRelativoToque: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var larguraPaginaPx by remember { mutableFloatStateOf(1f) }
    var alturaPaginaPx by remember { mutableFloatStateOf(1f) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        val proporcao = paginaRenderizada.proporcaoLarguraAltura.coerceAtLeast(0.1f)
        val alturaParaLarguraMaxima = maxWidth / proporcao
        val (larguraPdfDp, alturaPdfDp) = if (alturaParaLarguraMaxima <= maxHeight) {
            maxWidth to alturaParaLarguraMaxima
        } else {
            (maxHeight * proporcao) to maxHeight
        }

        Card(
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .size(width = larguraPdfDp, height = alturaPdfDp)
                .clipToBounds()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { tamanho ->
                        larguraPaginaPx = tamanho.width.toFloat().coerceAtLeast(1f)
                        alturaPaginaPx = tamanho.height.toFloat().coerceAtLeast(1f)
                    }
                    .pointerInput(larguraPaginaPx, alturaPaginaPx) {
                        detectTapGestures { offsetToque ->
                            val xRelativo = (offsetToque.x / larguraPaginaPx).coerceIn(0f, 1f)
                            val yRelativo = (offsetToque.y / alturaPaginaPx).coerceIn(0f, 1f)
                            aoTocarNaPaginaParaInserir(xRelativo, yRelativo)
                        }
                    }
            ) {
                Image(
                    bitmap = paginaRenderizada.bitmap.asImageBitmap(),
                    contentDescription = "Página ${paginaRenderizada.indicePagina + 1} do PDF",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )

                camposDaPagina.forEach { campo ->
                    CaixaAssinaturaArrastavel(
                        campo = campo,
                        estaSelecionado = campo.id == campoSelecionadoId,
                        larguraPaginaPx = larguraPaginaPx,
                        alturaPaginaPx = alturaPaginaPx,
                        density = density,
                        aoSelecionar = { aoSelecionarCampo(campo.id) },
                        aoArrastarNormalizado = { novoX, novoY ->
                            aoAtualizarPosicaoNormalizada(campo.id, novoX, novoY)
                        },
                        aoAlternarDimensao = { aoAlternarDimensaoCampo(campo.id) },
                        aoExcluir = { aoExcluirCampo(campo.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CaixaAssinaturaArrastavel(
    campo: CampoAssinaturaPosicionadoUi,
    estaSelecionado: Boolean,
    larguraPaginaPx: Float,
    alturaPaginaPx: Float,
    density: androidx.compose.ui.unit.Density,
    aoSelecionar: () -> Unit,
    aoArrastarNormalizado: (novoXRelativo: Float, novoYRelativo: Float) -> Unit,
    aoAlternarDimensao: () -> Unit,
    aoExcluir: () -> Unit
) {
    val corSignatario = obterCorSignatario(campo.indiceCor)

    // Mantém referências sempre atualizadas durante o gesto contínuo de arraste (Drag)
    val campoAtualState by rememberUpdatedState(campo)
    val callbackArrastar by rememberUpdatedState(aoArrastarNormalizado)
    val callbackSelecionar by rememberUpdatedState(aoSelecionar)

    val offsetXPx = (campo.posicaoXRelativa * larguraPaginaPx).roundToInt()
    val offsetYPx = (campo.posicaoYRelativa * alturaPaginaPx).roundToInt()

    val larguraDp = with(density) { (campo.larguraRelativa * larguraPaginaPx).toDp() }
    val alturaDp = with(density) { (campo.alturaRelativa * alturaPaginaPx).toDp() }

    Box(
        modifier = Modifier
            .offset { IntOffset(x = offsetXPx, y = offsetYPx) }
            .size(width = larguraDp, height = alturaDp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                corSignatario.copy(alpha = if (estaSelecionado) 0.24f else 0.16f)
            )
            .border(
                width = if (estaSelecionado) 2.2.dp else 1.5.dp,
                color = corSignatario,
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(campo.id, larguraPaginaPx, alturaPaginaPx) {
                detectDragGestures(
                    onDragStart = { callbackSelecionar() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val atual = campoAtualState

                        val deltaXRelativo = dragAmount.x / larguraPaginaPx
                        val deltaYRelativo = dragAmount.y / alturaPaginaPx

                        val limiteMaxX = (1f - atual.larguraRelativa).coerceAtLeast(0f)
                        val limiteMaxY = (1f - atual.alturaRelativa).coerceAtLeast(0f)

                        val novoX = (atual.posicaoXRelativa + deltaXRelativo)
                            .coerceIn(0f, limiteMaxX)
                        val novoY = (atual.posicaoYRelativa + deltaYRelativo)
                            .coerceIn(0f, limiteMaxY)

                        callbackArrastar(novoX, novoY)
                    }
                )
            }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabeçalho da caixa: Ícone de arraste + Nome do Signatário + Botões Redimensionar e Excluir
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenWith,
                        contentDescription = "Arrastar campo",
                        tint = corSignatario,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = campo.nomeSignatario,
                        color = corSignatario,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Alterna dimensão relativa (Compacto / Padrão / Amplo)
                    IconButton(
                        onClick = aoAlternarDimensao,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = "Ajustar dimensão da caixa",
                            tint = corSignatario,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Excluir este campo de assinatura
                    Surface(
                        color = corSignatario,
                        shape = CircleShape,
                        modifier = Modifier.size(18.dp)
                    ) {
                        IconButton(
                            onClick = aoExcluir,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Excluir campo de assinatura",
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }

            // Rodapé da caixa: Papel do signatário
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Draw,
                    contentDescription = null,
                    tint = corSignatario,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = campo.papelSignatario.descricaoExibicao,
                    color = corSignatario,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

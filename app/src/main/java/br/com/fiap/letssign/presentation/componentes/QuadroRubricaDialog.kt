package br.com.fiap.letssign.presentation.componentes

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.fiap.letssign.core.ui.theme.FiapPinkAccent
import br.com.fiap.letssign.core.ui.theme.LetsSignPurpleDark
import br.com.fiap.letssign.core.ui.theme.LetsSignPurplePrimary

private data class TracoAssinatura(
    val pontos: List<Offset>
)

// Quadro de captura de assinatura manuscrita via Canvas
@Composable
fun QuadroRubricaDialog(
    nomeSignatario: String,
    papelSignatario: String,
    quantidadeCamposParaCarimbar: Int,
    aoConfirmarAssinatura: (Bitmap) -> Unit,
    aoCancelar: () -> Unit
) {
    val tracosConcluidos = remember { mutableStateListOf<TracoAssinatura>() }
    val pontosTracoAtual = remember { mutableStateListOf<Offset>() }

    var larguraCanvasPx by remember { mutableIntStateOf(600) }
    var alturaCanvasPx by remember { mutableIntStateOf(240) }
    var mensagemAviso by remember { mutableStateOf<String?>(null) }

    val totalPontosDesenhados = tracosConcluidos.sumOf { it.pontos.size } + pontosTracoAtual.size

    AlertDialog(
        onDismissRequest = aoCancelar,
        icon = {
            Icon(
                imageVector = Icons.Default.Draw,
                contentDescription = null,
                tint = LetsSignPurplePrimary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Desenhe sua Assinatura",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "$nomeSignatario ($papelSignatario)",
                    style = MaterialTheme.typography.labelLarge,
                    color = LetsSignPurplePrimary
                )
                Text(
                    text = "Sua assinatura será carimbada em $quantidadeCamposParaCarimbar campo(s) demarcado(s) no PDF.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Área do Canvas de Assinatura Manuscrita
                Card(
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, LetsSignPurplePrimary),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { tamanho ->
                                larguraCanvasPx = tamanho.width.coerceAtLeast(200)
                                alturaCanvasPx = tamanho.height.coerceAtLeast(100)
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offsetInicial ->
                                        mensagemAviso = null
                                        pontosTracoAtual.clear()
                                        pontosTracoAtual.add(offsetInicial)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        pontosTracoAtual.add(change.position)
                                    },
                                    onDragEnd = {
                                        if (pontosTracoAtual.isNotEmpty()) {
                                            tracosConcluidos.add(
                                                TracoAssinatura(pontos = pontosTracoAtual.toList())
                                            )
                                            pontosTracoAtual.clear()
                                        }
                                    }
                                )
                            }
                    ) {
                        // Guia pontilhada / linha de base para assinatura
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 34.dp)
                                .height(1.dp)
                                .background(Color.LightGray)
                                .align(Alignment.BottomCenter)
                        )

                        if (totalPontosDesenhados == 0) {
                            Text(
                                text = "Assine com o dedo aqui",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val estiloTraco = Stroke(
                                width = 6.5f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )

                            tracosConcluidos.forEach { traco ->
                                desenharCaminhoSuavizado(traco.pontos, LetsSignPurpleDark, estiloTraco)
                            }

                            if (pontosTracoAtual.isNotEmpty()) {
                                desenharCaminhoSuavizado(pontosTracoAtual, LetsSignPurpleDark, estiloTraco)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            tracosConcluidos.clear()
                            pontosTracoAtual.clear()
                            mensagemAviso = null
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Limpar Quadro")
                    }

                    if (mensagemAviso != null) {
                        Text(
                            text = mensagemAviso.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (totalPontosDesenhados < 5) {
                        mensagemAviso = "Desenhe sua assinatura antes de confirmar."
                    } else {
                        val todosOsTracos = buildList {
                            addAll(tracosConcluidos)
                            if (pontosTracoAtual.isNotEmpty()) {
                                add(TracoAssinatura(pontosTracoAtual.toList()))
                            }
                        }
                        val bitmapGerado = exportarTracosParaBitmap(
                            tracos = todosOsTracos,
                            larguraPx = larguraCanvasPx,
                            alturaPx = alturaCanvasPx
                        )
                        aoConfirmarAssinatura(bitmapGerado)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FiapPinkAccent)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Carimbar e Assinar PDF", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = aoCancelar) {
                Text("Cancelar")
            }
        }
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.desenharCaminhoSuavizado(
    pontos: List<Offset>,
    cor: Color,
    estilo: Stroke
) {
    if (pontos.isEmpty()) return
    val path = Path().apply {
        moveTo(pontos.first().x, pontos.first().y)
        for (i in 1 until pontos.size) {
            val anterior = pontos[i - 1]
            val atual = pontos[i]
            val pontoMedioX = (anterior.x + atual.x) / 2f
            val pontoMedioY = (anterior.y + atual.y) / 2f
            quadraticTo(anterior.x, anterior.y, pontoMedioX, pontoMedioY)
        }
        lineTo(pontos.last().x, pontos.last().y)
    }
    drawPath(path = path, color = cor, style = estilo)
}

private fun exportarTracosParaBitmap(
    tracos: List<TracoAssinatura>,
    larguraPx: Int,
    alturaPx: Int
): Bitmap {
    val bitmap = Bitmap.createBitmap(
        larguraPx.coerceAtLeast(200),
        alturaPx.coerceAtLeast(100),
        Bitmap.Config.ARGB_8888
    )
    val canvasAndroid = android.graphics.Canvas(bitmap)
    val paint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.rgb(27, 10, 96) // Azul/Roxo escuro estilo caneta esferográfica jurídica
        style = Paint.Style.STROKE
        strokeWidth = 7f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    for (traco in tracos) {
        val pts = traco.pontos
        if (pts.isEmpty()) continue
        val path = android.graphics.Path()
        path.moveTo(pts.first().x, pts.first().y)
        for (i in 1 until pts.size) {
            val ant = pts[i - 1]
            val atu = pts[i]
            path.quadTo(ant.x, ant.y, (ant.x + atu.x) / 2f, (ant.y + atu.y) / 2f)
        }
        path.lineTo(pts.last().x, pts.last().y)
        canvasAndroid.drawPath(path, paint)
    }

    return bitmap
}

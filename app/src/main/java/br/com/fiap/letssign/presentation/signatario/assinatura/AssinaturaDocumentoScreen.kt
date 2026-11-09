package br.com.fiap.letssign.presentation.signatario.assinatura

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.core.ui.theme.FiapPinkAccent
import br.com.fiap.letssign.core.ui.theme.LetsSignPurplePrimary
import br.com.fiap.letssign.core.ui.theme.StatusSuccessGreen
import br.com.fiap.letssign.domain.model.Documento
import br.com.fiap.letssign.domain.model.StatusDocumento
import br.com.fiap.letssign.presentation.componentes.QuadroRubricaDialog
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssinaturaDocumentoScreen(
    viewModel: AssinaturaViewModel,
    aoVoltarAoDashboard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.detalhes?.documento?.titulo ?: "Assinatura Digital",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val subtitulo = uiState.participacaoSignatario?.let {
                            "${it.nomeSignatario} • Papel: ${it.papel.descricaoExibicao}"
                        } ?: "Carregando..."
                        Text(
                            text = subtitulo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = aoVoltarAoDashboard,
                        enabled = !uiState.processandoAssinaturaPdfBox
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar ao Dashboard",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignPurplePrimary
                )
            )
        }
    ) { innerPadding ->
        if (uiState.carregandoDados) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val totalPaginas = uiState.detalhes?.documento?.totalPaginas ?: 1
            val campoFocado = uiState.campoAtualmenteFocado

            var larguraPaginaPx by remember { mutableFloatStateOf(1f) }
            var alturaPaginaPx by remember { mutableFloatStateOf(1f) }
            val density = LocalDensity.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // 1. Painel de Foco nas Coordenadas (X, Y) do Signatário
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (uiState.jaAssinadoPorEsteSignatario) {
                                        Icons.Default.CheckCircle
                                    } else {
                                        Icons.Default.CenterFocusStrong
                                    },
                                    contentDescription = null,
                                    tint = if (uiState.jaAssinadoPorEsteSignatario) {
                                        StatusSuccessGreen
                                    } else {
                                        FiapPinkAccent
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (uiState.jaAssinadoPorEsteSignatario) {
                                            "Assinatura já carimbada neste PDF"
                                        } else {
                                            "Foco no Campo ${uiState.indiceCampoFocado + 1} de ${uiState.camposDoSignatario.size}"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (campoFocado != null) {
                                        Text(
                                            text = "Campo posicionado na Página ${campoFocado.pagina + 1}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (uiState.camposDoSignatario.size > 1) {
                                OutlinedButton(
                                    onClick = viewModel::focarNoProximoCampoDeAssinatura,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Próximo Campo")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Controles de paginação do PDF
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = viewModel::irParaPaginaAnterior,
                                    enabled = uiState.paginaAtualPdf > 0,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior")
                                }
                                Text(
                                    text = "Página ${uiState.paginaAtualPdf + 1} de $totalPaginas",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                IconButton(
                                    onClick = viewModel::irParaProximaPagina,
                                    enabled = uiState.paginaAtualPdf < totalPaginas - 1,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Próxima")
                                }
                            }

                            if (!uiState.jaAssinadoPorEsteSignatario) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = null,
                                        tint = FiapPinkAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Toque na caixa destacada para assinar",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FiapPinkAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = uiState.erro != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.erro.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Renderização da Página do PDF com destaque na coordenada (X, Y) do Signatário
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    val pagina = uiState.paginaRenderizada
                    if (uiState.renderizandoPdf || pagina == null) {
                        CircularProgressIndicator()
                    } else {
                        val proporcao = pagina.proporcaoLarguraAltura.coerceAtLeast(0.1f)
                        val larguraPorAltura = maxHeight * proporcao
                        val (larguraPdfDp, alturaPdfDp) = if (larguraPorAltura <= maxWidth) {
                            larguraPorAltura to maxHeight
                        } else {
                            maxWidth to (maxWidth / proporcao)
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
                            ) {
                                Image(
                                    bitmap = pagina.bitmap.asImageBitmap(),
                                    contentDescription = "Página do PDF para assinatura",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Destaque interativo nos campos de assinatura do Signatário nesta página
                                uiState.camposDoSignatarioNaPaginaAtual.forEach { campo ->
                                    val estaFocado = campo.id == campoFocado?.id
                                    val offsetX = (campo.posicaoXRelativa * larguraPaginaPx).roundToInt()
                                    val offsetY = (campo.posicaoYRelativa * alturaPaginaPx).roundToInt()
                                    val largDp = with(density) { (campo.larguraRelativa * larguraPaginaPx).toDp() }
                                    val altDp = with(density) { (campo.alturaRelativa * alturaPaginaPx).toDp() }

                                    val corCaixa = if (campo.assinado || uiState.jaAssinadoPorEsteSignatario) {
                                        StatusSuccessGreen
                                    } else if (estaFocado) {
                                        FiapPinkAccent
                                    } else {
                                        LetsSignPurplePrimary
                                    }

                                    Box(
                                        modifier = Modifier
                                            .offset { IntOffset(offsetX, offsetY) }
                                            .size(largDp, altDp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (campo.assinado || uiState.jaAssinadoPorEsteSignatario) {
                                                    Color.Transparent
                                                } else {
                                                    corCaixa.copy(alpha = 0.20f)
                                                }
                                            )
                                            .border(
                                                width = if (estaFocado) 2.4.dp else 1.6.dp,
                                                color = corCaixa,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(enabled = !uiState.jaAssinadoPorEsteSignatario) {
                                                viewModel.abrirQuadroRubrica()
                                            }
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!campo.assinado && !uiState.jaAssinadoPorEsteSignatario) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Draw,
                                                    contentDescription = null,
                                                    tint = corCaixa,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "ASSINAR AQUI",
                                                    color = corCaixa,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Botão Principal de Ação no Rodapé
                if (!uiState.jaAssinadoPorEsteSignatario) {
                    Button(
                        onClick = viewModel::abrirQuadroRubrica,
                        enabled = !uiState.processandoAssinaturaPdfBox,
                        colors = ButtonDefaults.buttonColors(containerColor = FiapPinkAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        if (uiState.processandoAssinaturaPdfBox) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Carimbando PDF via PDFBox...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Draw,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Desenhar Assinatura e Concluir",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = aoVoltarAoDashboard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Voltar ao Dashboard do Signatário")
                    }
                }
            }
        }

        // Quadro de Desenho da Assinatura Manuscrita (Canvas Nativo)
        if (uiState.exibirQuadroRubricaDialog) {
            val participacao = uiState.participacaoSignatario
            QuadroRubricaDialog(
                nomeSignatario = participacao?.nomeSignatario ?: "Signatário",
                papelSignatario = participacao?.papel?.descricaoExibicao ?: "Participante",
                quantidadeCamposParaCarimbar = uiState.camposDoSignatario.size,
                aoConfirmarAssinatura = viewModel::confirmarAssinaturaNoPdf,
                aoCancelar = viewModel::fecharQuadroRubrica
            )
        }

        // Modal de Sucesso após carimbar o PDF e atualizar o Room
        uiState.documentoAssinadoComSucesso?.let { docAtualizado ->
            DialogSucessoAssinaturaSignatario(
                documento = docAtualizado,
                aoConcluirEVoltar = aoVoltarAoDashboard
            )
        }
    }
}

@Composable
private fun DialogSucessoAssinaturaSignatario(
    documento: Documento,
    aoConcluirEVoltar: () -> Unit
) {
    val todosConcluiram = documento.status == StatusDocumento.CONCLUIDO

    AlertDialog(
        onDismissRequest = aoConcluirEVoltar,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = StatusSuccessGreen,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Assinatura Registrada com Sucesso!",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Sua assinatura manuscrita foi estampada permanentemente nas coordenadas demarcadas do arquivo PDF.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (todosConcluiram) {
                        "Status do Documento: CONCLUÍDO (Todos os signatários obrigatórios finalizaram suas assinaturas)."
                    } else {
                        "Status do Documento: PARCIALMENTE ASSINADO (Sua assinatura foi concluída; aguardando demais signatários)."
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (todosConcluiram) StatusSuccessGreen else LetsSignPurplePrimary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = aoConcluirEVoltar,
                colors = ButtonDefaults.buttonColors(containerColor = LetsSignPurplePrimary)
            ) {
                Text("Retornar ao Dashboard")
            }
        }
    )
}

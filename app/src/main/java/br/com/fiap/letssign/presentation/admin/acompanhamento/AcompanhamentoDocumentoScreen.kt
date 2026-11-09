package br.com.fiap.letssign.presentation.admin.acompanhamento

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.core.comum.FormatadorData
import br.com.fiap.letssign.core.ui.theme.FiapPinkAccent
import br.com.fiap.letssign.core.ui.theme.LetsSignPurplePrimary
import br.com.fiap.letssign.core.ui.theme.StatusSentBlue
import br.com.fiap.letssign.core.ui.theme.StatusSuccessGreen
import br.com.fiap.letssign.core.ui.theme.StatusWarningAmber
import br.com.fiap.letssign.domain.model.CampoAssinatura
import br.com.fiap.letssign.domain.model.DetalhesAcompanhamentoDocumento
import br.com.fiap.letssign.domain.model.EventoDocumento
import br.com.fiap.letssign.domain.model.SignatarioDocumento
import br.com.fiap.letssign.domain.model.StatusAssinatura
import br.com.fiap.letssign.domain.model.StatusDocumento
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcompanhamentoDocumentoScreen(
    viewModel: AcompanhamentoViewModel,
    aoVoltar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Acompanhamento de Documento",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.detalhes?.documento?.titulo ?: "Carregando...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
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
        val detalhes = uiState.detalhes

        if (uiState.carregando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (detalhes == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Documento não encontrado.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                TabRow(
                    selectedTabIndex = uiState.abaSelecionada.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    AbaAcompanhamento.entries.forEach { aba ->
                        Tab(
                            selected = uiState.abaSelecionada == aba,
                            onClick = { viewModel.selecionarAba(aba) },
                            text = {
                                Text(
                                    text = aba.titulo,
                                    fontWeight = if (uiState.abaSelecionada == aba) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            }
                        )
                    }
                }

                when (uiState.abaSelecionada) {
                    AbaAcompanhamento.RESUMO_E_AUDITORIA -> {
                        ConteudoResumoEAuditoria(detalhes = detalhes)
                    }

                    AbaAcompanhamento.VISUALIZAR_PDF -> {
                        ConteudoVisualizacaoPdfAcompanhamento(
                            uiState = uiState,
                            detalhes = detalhes,
                            aoPaginaAnterior = viewModel::irParaPaginaAnterior,
                            aoProximaPagina = viewModel::irParaProximaPagina
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConteudoResumoEAuditoria(
    detalhes: DetalhesAcompanhamentoDocumento
) {
    val doc = detalhes.documento
    val totalSignatarios = detalhes.signatarios.size
    val totalAssinados = detalhes.signatarios.count { it.status == StatusAssinatura.ASSINADO }

    val corStatusGeral = when (doc.status) {
        StatusDocumento.ENVIADO -> StatusSentBlue
        StatusDocumento.PARCIALMENTE_ASSINADO -> StatusWarningAmber
        StatusDocumento.CONCLUIDO -> StatusSuccessGreen
        StatusDocumento.RASCUNHO -> Color.Gray
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Situação Geral do Documento
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = LetsSignPurplePrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = doc.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = corStatusGeral.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = doc.status.tituloExibicao,
                                color = corStatusGeral,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val progresso = if (totalSignatarios > 0) {
                        totalAssinados.toFloat() / totalSignatarios.toFloat()
                    } else {
                        0f
                    }
                    LinearProgressIndicator(
                        progress = { progresso },
                        color = corStatusGeral,
                        trackColor = corStatusGeral.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$totalAssinados de $totalSignatarios assinatura(s) realizada(s) • ${doc.totalPaginas} página(s)",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Criado em: ${FormatadorData.formatarDataHora(doc.criadoEm)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Enviado em: ${FormatadorData.formatarDataHora(doc.enviadoEm)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (doc.concluidoEm != null) {
                        Text(
                            text = "Concluído em: ${FormatadorData.formatarDataHora(doc.concluidoEm)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusSuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (doc.mensagemConvite.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Mensagem do Convite: \"${doc.mensagemConvite}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = FiapPinkAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Versão v${doc.versao} • Documento enviado e imutável",
                            style = MaterialTheme.typography.labelLarge,
                            color = FiapPinkAccent
                        )
                    }
                }
            }
        }

        // 2. Situação Individual dos Signatários
        item {
            Text(
                text = "Situação Individual dos Signatários (${detalhes.signatarios.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(detalhes.signatarios, key = { it.id }) { signatario ->
            val camposParticipante = detalhes.camposAssinatura.filter {
                it.signatarioDocumentoId == signatario.id
            }
            CardSituacaoSignatario(
                signatario = signatario,
                campos = camposParticipante
            )
        }

        // 3. Histórico de Eventos (Trilha de Auditoria)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = LetsSignPurplePrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Histórico de Eventos e Auditoria",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(detalhes.historicoEventos, key = { it.id }) { evento ->
            ItemTimelineEvento(evento = evento)
        }
    }
}

@Composable
private fun CardSituacaoSignatario(
    signatario: SignatarioDocumento,
    campos: List<CampoAssinatura>
) {
    val assinado = signatario.status == StatusAssinatura.ASSINADO
    val corStatus = if (assinado) StatusSuccessGreen else StatusWarningAmber

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = LetsSignPurplePrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = signatario.nomeSignatario,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = signatario.papel.descricaoExibicao,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = corStatus.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (assinado) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = corStatus,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = signatario.status.descricaoExibicao,
                            color = corStatus,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${signatario.emailSignatario} • ${signatario.telefoneSignatario}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val paginas = campos.map { it.pagina + 1 }.distinct().sorted().joinToString(", ")
            Text(
                text = "${campos.size} campo(s) posicionado(s) na(s) página(s): ${paginas.ifBlank { "-" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (assinado && signatario.assinadoEm != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Assinado em ${FormatadorData.formatarDataHora(signatario.assinadoEm)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = StatusSuccessGreen
                )
            }
        }
    }
}

@Composable
private fun ItemTimelineEvento(evento: EventoDocumento) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(LetsSignPurplePrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = LetsSignPurplePrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = evento.tipoEvento.descricaoExibicao,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = FormatadorData.formatarDataHora(evento.dataHora),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = evento.descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ConteudoVisualizacaoPdfAcompanhamento(
    uiState: AcompanhamentoUiState,
    detalhes: DetalhesAcompanhamentoDocumento,
    aoPaginaAnterior: () -> Unit,
    aoProximaPagina: () -> Unit
) {
    val totalPaginas = detalhes.documento.totalPaginas
    val camposDaPagina = detalhes.camposAssinatura.filter { it.pagina == uiState.paginaAtualPdf }
    val mapaSignatariosPorParticipacaoId = remember(detalhes.signatarios) {
        detalhes.signatarios.associateBy { it.id }
    }

    var larguraPx by remember { mutableFloatStateOf(1f) }
    var alturaPx by remember { mutableFloatStateOf(1f) }
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra de paginação do PDF
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = aoPaginaAnterior,
                    enabled = uiState.paginaAtualPdf > 0
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Página anterior")
                }

                Text(
                    text = "Página ${uiState.paginaAtualPdf + 1} de $totalPaginas • ${detalhes.documento.status.tituloExibicao}",
                    style = MaterialTheme.typography.labelLarge
                )

                IconButton(
                    onClick = aoProximaPagina,
                    enabled = uiState.paginaAtualPdf < totalPaginas - 1
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Próxima página")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .size(width = larguraPdfDp, height = alturaPdfDp)
                        .clipToBounds()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged {
                                larguraPx = it.width.toFloat().coerceAtLeast(1f)
                                alturaPx = it.height.toFloat().coerceAtLeast(1f)
                            }
                    ) {
                        Image(
                            bitmap = pagina.bitmap.asImageBitmap(),
                            contentDescription = "PDF Final",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Sobrepõe indicação discreta dos campos de assinatura (Pendentes ou Assinados)
                        camposDaPagina.forEach { campo ->
                            val signatario = mapaSignatariosPorParticipacaoId[campo.signatarioDocumentoId]
                            val cor = if (campo.assinado) StatusSuccessGreen else StatusWarningAmber
                            val offsetX = (campo.posicaoXRelativa * larguraPx).roundToInt()
                            val offsetY = (campo.posicaoYRelativa * alturaPx).roundToInt()
                            val largDp = with(density) { (campo.larguraRelativa * larguraPx).toDp() }
                            val altDp = with(density) { (campo.alturaRelativa * alturaPx).toDp() }

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(offsetX, offsetY) }
                                    .size(largDp, altDp)
                                    .border(1.5.dp, cor, RoundedCornerShape(6.dp))
                                    .background(
                                        cor.copy(alpha = if (campo.assinado) 0.08f else 0.16f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Text(
                                    text = "${signatario?.nomeSignatario?.substringBefore(" ") ?: "Signatário"} • ${if (campo.assinado) "Assinado" else "Pendente"}",
                                    color = cor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

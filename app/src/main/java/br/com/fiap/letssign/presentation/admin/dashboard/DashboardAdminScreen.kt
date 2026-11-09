package br.com.fiap.letssign.presentation.admin.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.core.comum.FormatadorData
import br.com.fiap.letssign.core.ui.theme.FiapPinkAccent
import br.com.fiap.letssign.core.ui.theme.LetsSignPurplePrimary
import br.com.fiap.letssign.core.ui.theme.StatusSentBlue
import br.com.fiap.letssign.core.ui.theme.StatusSuccessGreen
import br.com.fiap.letssign.core.ui.theme.StatusWarningAmber
import br.com.fiap.letssign.domain.model.ResumoDocumentoAdmin
import br.com.fiap.letssign.domain.model.StatusDocumento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAdminScreen(
    viewModel: DashboardAdminViewModel,
    aoIrParaGestaoSignatarios: () -> Unit,
    aoCriarNovoDocumento: () -> Unit,
    aoAbrirAcompanhamentoDocumento: (String) -> Unit,
    aoSairDaConta: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val abasStatus = listOf(
        StatusDocumento.ENVIADO,
        StatusDocumento.PARCIALMENTE_ASSINADO,
        StatusDocumento.CONCLUIDO
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "LetsSign • Admin",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gestão e Envio de Documentos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = aoIrParaGestaoSignatarios) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Gerenciar Signatários",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { viewModel.realizarLogout(aoSairDaConta) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sair da conta",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetsSignPurplePrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = aoCriarNovoDocumento,
                containerColor = FiapPinkAccent,
                contentColor = Color.White,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Novo Documento"
                    )
                },
                text = { Text("Novo Documento", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Card de Atalho para Gerenciar Signatários + Resumo Rápido
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Base de Signatários",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cadastre ou edite participantes que assinarão seus PDFs.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = aoIrParaGestaoSignatarios,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gerenciar")
                    }
                }
            }

            // Abas por Status (Enviados, Parcialmente Assinados, Concluídos)
            val indiceSelecionado = abasStatus.indexOf(uiState.abaSelecionada).coerceAtLeast(0)
            ScrollableTabRow(
                selectedTabIndex = indiceSelecionado,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                abasStatus.forEach { status ->
                    val quantidade = when (status) {
                        StatusDocumento.ENVIADO -> uiState.documentosEnviados.size
                        StatusDocumento.PARCIALMENTE_ASSINADO -> uiState.documentosParcialmenteAssinados.size
                        StatusDocumento.CONCLUIDO -> uiState.documentosConcluidos.size
                        else -> 0
                    }
                    Tab(
                        selected = uiState.abaSelecionada == status,
                        onClick = { viewModel.selecionarAba(status) },
                        text = {
                            Text(
                                text = "${status.tituloExibicao} ($quantidade)",
                                fontWeight = if (uiState.abaSelecionada == status) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    )
                }
            }

            // Lista de Documentos da Aba Selecionada
            if (uiState.carregando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.documentosDaAbaAtual.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhum documento em '${uiState.abaSelecionada.tituloExibicao}'",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Toque em 'Novo Documento' para importar um PDF e coletar assinaturas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 14.dp,
                        bottom = 96.dp
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.documentosDaAbaAtual, key = { it.documento.id }) { item ->
                        CardDocumentoAdmin(
                            resumo = item,
                            aoClicar = { aoAbrirAcompanhamentoDocumento(item.documento.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardDocumentoAdmin(
    resumo: ResumoDocumentoAdmin,
    aoClicar: () -> Unit
) {
    val corStatus = when (resumo.documento.status) {
        StatusDocumento.ENVIADO -> StatusSentBlue
        StatusDocumento.PARCIALMENTE_ASSINADO -> StatusWarningAmber
        StatusDocumento.CONCLUIDO -> StatusSuccessGreen
        StatusDocumento.RASCUNHO -> Color.Gray
    }

    val iconeStatus = when (resumo.documento.status) {
        StatusDocumento.ENVIADO -> Icons.Default.Send
        StatusDocumento.PARCIALMENTE_ASSINADO -> Icons.Default.HourglassTop
        StatusDocumento.CONCLUIDO -> Icons.Default.CheckCircle
        StatusDocumento.RASCUNHO -> Icons.Default.Description
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = aoClicar)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
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
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = resumo.documento.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                            imageVector = iconeStatus,
                            contentDescription = null,
                            tint = corStatus,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = resumo.documento.status.tituloExibicao,
                            color = corStatus,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Participantes: ${resumo.nomesSignatarios.joinToString(", ").ifBlank { "-" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            val progresso = if (resumo.totalSignatarios > 0) {
                resumo.totalAssinados.toFloat() / resumo.totalSignatarios.toFloat()
            } else {
                0f
            }

            LinearProgressIndicator(
                progress = { progresso },
                color = corStatus,
                trackColor = corStatus.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${resumo.totalAssinados} de ${resumo.totalSignatarios} assinatura(s)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Enviado em ${FormatadorData.formatarDataHora(resumo.documento.enviadoEm)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

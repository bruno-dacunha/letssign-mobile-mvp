package br.com.fiap.letssign.presentation.signatario.dashboard

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.PendingActions
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import br.com.fiap.letssign.core.ui.theme.StatusSuccessGreen
import br.com.fiap.letssign.core.ui.theme.StatusWarningAmber
import br.com.fiap.letssign.domain.model.ResumoDocumentoSignatario
import br.com.fiap.letssign.domain.model.StatusAssinatura

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardSignatarioScreen(
    viewModel: DashboardSignatarioViewModel,
    aoAbrirDocumentoParaAssinar: (documentoId: String) -> Unit,
    aoSairDaConta: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.usuarioSignatario?.nomeCompleto ?: "Área do Signatário",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.usuarioSignatario?.email ?: "Documentos para Assinatura",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.realizarLogout(aoSairDaConta) }) {
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = if (uiState.abaSelecionada == StatusAssinatura.PENDENTE) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = uiState.abaSelecionada == StatusAssinatura.PENDENTE,
                    onClick = { viewModel.selecionarAba(StatusAssinatura.PENDENTE) },
                    text = {
                        Text(
                            text = "Pendentes (${uiState.documentosPendentes.size})",
                            fontWeight = if (uiState.abaSelecionada == StatusAssinatura.PENDENTE) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    }
                )
                Tab(
                    selected = uiState.abaSelecionada == StatusAssinatura.ASSINADO,
                    onClick = { viewModel.selecionarAba(StatusAssinatura.ASSINADO) },
                    text = {
                        Text(
                            text = "Assinados (${uiState.documentosAssinados.size})",
                            fontWeight = if (uiState.abaSelecionada == StatusAssinatura.ASSINADO) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    }
                )
            }

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
                            imageVector = if (uiState.abaSelecionada == StatusAssinatura.PENDENTE) {
                                Icons.Default.PendingActions
                            } else {
                                Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (uiState.abaSelecionada == StatusAssinatura.PENDENTE) {
                                "Nenhum documento pendente para assinar"
                            } else {
                                "Você ainda não assinou nenhum documento"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.documentosDaAbaAtual, key = { it.documento.id }) { item ->
                        CardDocumentoSignatario(
                            resumo = item,
                            aoAbrir = { aoAbrirDocumentoParaAssinar(item.documento.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardDocumentoSignatario(
    resumo: ResumoDocumentoSignatario,
    aoAbrir: () -> Unit
) {
    val pendente = resumo.signatarioDocumento.status == StatusAssinatura.PENDENTE
    val corDestaque = if (pendente) StatusWarningAmber else StatusSuccessGreen

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
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
                    color = corDestaque.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = resumo.signatarioDocumento.papel.descricaoExibicao,
                        color = corDestaque,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (resumo.documento.mensagemConvite.isNotBlank()) {
                Text(
                    text = "Mensagem: \"${resumo.documento.mensagemConvite}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = if (pendente) {
                    "${resumo.totalCamposParaAssinar} campo(s) demarcado(s) aguardando sua assinatura • Recebido em ${FormatadorData.formatarDataHora(resumo.documento.enviadoEm)}"
                } else {
                    "Assinado em ${FormatadorData.formatarDataHora(resumo.signatarioDocumento.assinadoEm)} • Status geral: ${resumo.documento.status.tituloExibicao}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (pendente) {
                Button(
                    onClick = aoAbrir,
                    colors = ButtonDefaults.buttonColors(containerColor = FiapPinkAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Draw,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Abrir PDF e Assinar", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = aoAbrir,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusSuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Visualizar Documento Assinado")
                }
            }
        }
    }
}

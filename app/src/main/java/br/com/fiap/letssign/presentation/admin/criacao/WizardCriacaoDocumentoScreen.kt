package br.com.fiap.letssign.presentation.admin.criacao

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fiap.letssign.core.ui.theme.FiapPinkAccent
import br.com.fiap.letssign.core.ui.theme.LetsSignPurplePrimary
import br.com.fiap.letssign.core.ui.theme.StatusSuccessGreen
import br.com.fiap.letssign.domain.model.Documento
import br.com.fiap.letssign.domain.model.PapelSignatario
import br.com.fiap.letssign.domain.model.Usuario
import br.com.fiap.letssign.presentation.componentes.VisualizadorPaginaPdfDragDrop
import br.com.fiap.letssign.presentation.componentes.obterCorSignatario
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardCriacaoDocumentoScreen(
    viewModel: CriacaoDocumentoViewModel,
    aoVoltarAoDashboard: () -> Unit,
    aoAbrirCadastroRapidoSignatario: () -> Unit,
    aoIrParaAcompanhamentoDocumento: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = !uiState.enviandoDocumento) {
        viewModel.voltarEtapaAnterior(aoSairDoWizard = aoVoltarAoDashboard)
    }

    val selecionadorPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.aoSelecionarArquivoPdfNoCelular(it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Novo Documento para Assinatura",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Passo ${uiState.etapaAtual.numero} de 4 • ${uiState.etapaAtual.tituloCurto}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.voltarEtapaAnterior(aoVoltarAoDashboard) },
                        enabled = !uiState.enviandoDocumento
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
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
            if (uiState.etapaAtual != EtapaWizard.PASSO_3_POSICIONAMENTO) {
                BarraProgressoWizard(etapaAtual = uiState.etapaAtual)
            }

            when (uiState.etapaAtual) {
                EtapaWizard.PASSO_1_UPLOAD -> {
                    ConteudoPasso1UploadPdf(
                        uiState = uiState,
                        aoAlterarTitulo = viewModel::aoAlterarTituloDocumento,
                        aoAbrirFilePickerPdf = {
                            selecionadorPdfLauncher.launch(arrayOf("application/pdf"))
                        },
                        aoGerarPdfDemonstracao = viewModel::gerarPdfModeloParaTeste,
                        aoRemoverPdf = viewModel::removerPdfSelecionado,
                        aoAvancarPasso2 = viewModel::avancarDoPasso1ParaPasso2
                    )
                }

                EtapaWizard.PASSO_2_SIGNATARIOS -> {
                    ConteudoPasso2SelecaoSignatarios(
                        uiState = uiState,
                        aoAlterarBusca = viewModel::aoAlterarBuscaSignatario,
                        aoAlternarSignatario = viewModel::alternarSelecaoSignatario,
                        aoAlterarPapel = viewModel::alterarPapelDoSignatario,
                        aoRemoverSelecionado = viewModel::removerSignatarioSelecionado,
                        aoAbrirCadastroSignatario = aoAbrirCadastroRapidoSignatario,
                        aoVoltarPasso1 = {
                            viewModel.voltarEtapaAnterior(aoVoltarAoDashboard)
                        },
                        aoAvancarPasso3 = viewModel::avancarDoPasso2ParaPasso3
                    )
                }

                EtapaWizard.PASSO_3_POSICIONAMENTO -> {
                    ConteudoPasso3PosicionamentoDragDrop(
                        uiState = uiState,
                        aoSelecionarSignatarioAtivo = viewModel::selecionarSignatarioAtivoParaPosicionamento,
                        aoAdicionarCampoNaPagina = {
                            viewModel.adicionarCampoAssinaturaNaPaginaAtual()
                        },
                        aoTocarNaPaginaParaInserir = { xRel, yRel ->
                            viewModel.adicionarCampoAssinaturaNaPaginaAtual(
                                xRelativoToque = xRel,
                                yRelativoToque = yRel
                            )
                        },
                        aoSelecionarCampo = viewModel::selecionarCampoPosicionado,
                        aoAtualizarPosicaoCampo = viewModel::atualizarPosicaoCampoNormalizada,
                        aoAlternarDimensaoCampo = viewModel::alternarDimensaoCampo,
                        aoExcluirCampo = viewModel::excluirCampoAssinatura,
                        aoIrPaginaAnterior = viewModel::irParaPaginaAnteriorPdf,
                        aoIrProximaPagina = viewModel::irParaProximaPaginaPdf,
                        aoVoltarPasso2 = {
                            viewModel.voltarEtapaAnterior(aoVoltarAoDashboard)
                        },
                        aoAvancarPasso4 = viewModel::avancarDoPasso3ParaPasso4
                    )
                }

                EtapaWizard.PASSO_4_REVISAO -> {
                    ConteudoPasso4RevisaoEDisparo(
                        uiState = uiState,
                        aoAlterarMensagemConvite = viewModel::aoAlterarMensagemConvite,
                        aoVoltarPasso3 = {
                            viewModel.voltarEtapaAnterior(aoVoltarAoDashboard)
                        },
                        aoConfirmarEnvio = viewModel::confirmarEDispararDocumento
                    )
                }
            }
        }

        // Modal de Sucesso após a Persistência e Disparo no Room
        uiState.documentoEnviadoComSucesso?.let { documentoEnviado ->
            DialogSucessoDisparoDocumento(
                documento = documentoEnviado,
                totalSignatarios = uiState.signatariosSelecionados.size,
                totalCampos = uiState.camposPosicionados.size,
                aoVoltarAoDashboard = aoVoltarAoDashboard,
                aoAcompanharDocumento = {
                    aoIrParaAcompanhamentoDocumento(documentoEnviado.id)
                }
            )
        }
    }
}

@Composable
private fun BarraProgressoWizard(etapaAtual: EtapaWizard) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EtapaWizard.entries.forEach { etapa ->
                val concluida = etapa.numero < etapaAtual.numero
                val ativa = etapa == etapaAtual

                val corCirculo = when {
                    concluida -> StatusSuccessGreen
                    ativa -> LetsSignPurplePrimary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val corTexto = when {
                    concluida || ativa -> Color.White
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(corCirculo),
                        contentAlignment = Alignment.Center
                    ) {
                        if (concluida) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = etapa.numero.toString(),
                                color = corTexto,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = etapa.tituloCurto,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (ativa) FontWeight.Bold else FontWeight.Normal,
                        color = if (ativa) {
                            LetsSignPurplePrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ConteudoPasso1UploadPdf(
    uiState: CriacaoDocumentoUiState,
    aoAlterarTitulo: (String) -> Unit,
    aoAbrirFilePickerPdf: () -> Unit,
    aoGerarPdfDemonstracao: () -> Unit,
    aoRemoverPdf: () -> Unit,
    aoAvancarPasso2: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "1. Identificação e Upload do PDF",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Informe o título do documento e selecione um arquivo PDF íntegro no seu dispositivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = uiState.tituloDocumento,
                onValueChange = aoAlterarTitulo,
                label = { Text("Título do Documento *") },
                placeholder = { Text("Ex.: Contrato de Prestação de Serviços 2026") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            val pdf = uiState.arquivoPdfValidado
            if (uiState.processandoPdf) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Validando integridade e páginas do PDF...",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else if (pdf == null) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = LetsSignPurplePrimary.copy(alpha = 0.45f)
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = aoAbrirFilePickerPdf)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = LetsSignPurplePrimary,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Selecionar Arquivo PDF no Celular",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Apenas arquivos .PDF válidos, íntegros e sem proteção por senha são aceitos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = aoAbrirFilePickerPdf,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Escolher PDF do Dispositivo")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = aoGerarPdfDemonstracao,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gerar Contrato PDF Modelo (2 Páginas) p/ Teste")
                }
            } else {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.5.dp, StatusSuccessGreen),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StatusSuccessGreen.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = StatusSuccessGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pdf.nomeArquivoOriginal,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val tamanhoKb = String.format(
                                    Locale("pt", "BR"),
                                    "%.1f KB",
                                    pdf.tamanhoBytes / 1024.0
                                )
                                Text(
                                    text = "${pdf.totalPaginas} página(s) • $tamanhoKb",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = StatusSuccessGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusSuccessGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PDF Válido",
                                        color = StatusSuccessGreen,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = aoAbrirFilePickerPdf,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Substituir PDF")
                            }

                            OutlinedButton(
                                onClick = aoRemoverPdf,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Remover",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = uiState.erroPasso1 != null) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.erroPasso1.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = aoAvancarPasso2,
            enabled = !uiState.processandoPdf,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LetsSignPurplePrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Avançar para Seleção de Signatários",
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ConteudoPasso2SelecaoSignatarios(
    uiState: CriacaoDocumentoUiState,
    aoAlterarBusca: (String) -> Unit,
    aoAlternarSignatario: (Usuario) -> Unit,
    aoAlterarPapel: (String, PapelSignatario) -> Unit,
    aoRemoverSelecionado: (String) -> Unit,
    aoAbrirCadastroSignatario: () -> Unit,
    aoVoltarPasso1: () -> Unit,
    aoAvancarPasso3: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "2. Selecionar e Classificar Signatários",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Selecione no mínimo 1 signatário obrigatório e defina seu papel.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = aoAbrirCadastroSignatario,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Novo")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.termoBuscaSignatario,
            onValueChange = aoAlterarBusca,
            placeholder = { Text("Filtrar signatários por nome ou e-mail...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        val totalSelecionados = uiState.signatariosSelecionados.size
        Surface(
            color = if (totalSelecionados > 0) {
                LetsSignPurplePrimary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$totalSelecionados signatário(s) selecionado(s)",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (totalSelecionados > 0) {
                        LetsSignPurplePrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = "Mínimo exigido: 1 obrigatório",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(visible = uiState.erroPasso2 != null) {
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.erroPasso2.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.carregandoSignatarios) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(uiState.signatariosFiltradosPasso2, key = { it.id }) { usuario ->
                    val selecao = uiState.signatariosSelecionados[usuario.id]
                    ItemSelecaoSignatarioCard(
                        usuario = usuario,
                        selecao = selecao,
                        aoAlternarCheckbox = { aoAlternarSignatario(usuario) },
                        aoSelecionarPapel = { papel -> aoAlterarPapel(usuario.id, papel) },
                        aoRemover = { aoRemoverSelecionado(usuario.id) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = aoVoltarPasso1,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("Voltar")
            }

            Button(
                onClick = aoAvancarPasso3,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LetsSignPurplePrimary),
                modifier = Modifier
                    .weight(1.6f)
                    .height(52.dp)
            ) {
                Text("Posicionar Assinaturas", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun ItemSelecaoSignatarioCard(
    usuario: Usuario,
    selecao: SignatarioSelecionadoUi?,
    aoAlternarCheckbox: () -> Unit,
    aoSelecionarPapel: (PapelSignatario) -> Unit,
    aoRemover: () -> Unit
) {
    val estaSelecionado = selecao != null

    Card(
        shape = RoundedCornerShape(16.dp),
        border = if (estaSelecionado) {
            BorderStroke(1.5.dp, LetsSignPurplePrimary)
        } else {
            null
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = aoAlternarCheckbox)
            ) {
                Checkbox(
                    checked = estaSelecionado,
                    onCheckedChange = { aoAlternarCheckbox() }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = usuario.nomeCompleto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${usuario.email} • ${usuario.telefone}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (estaSelecionado) {
                    IconButton(onClick = aoRemover) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remover signatário do documento",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            AnimatedVisibility(visible = selecao != null) {
                if (selecao != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Classificação do Signatário no Documento:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PapelSignatario.entries.forEach { papel ->
                                val ativo = selecao.papel == papel
                                FilterChip(
                                    selected = ativo,
                                    onClick = { aoSelecionarPapel(papel) },
                                    label = { Text(papel.descricaoExibicao) },
                                    leadingIcon = if (ativo) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LetsSignPurplePrimary.copy(alpha = 0.15f),
                                        selectedLabelColor = LetsSignPurplePrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConteudoPasso3PosicionamentoDragDrop(
    uiState: CriacaoDocumentoUiState,
    aoSelecionarSignatarioAtivo: (String) -> Unit,
    aoAdicionarCampoNaPagina: () -> Unit,
    aoTocarNaPaginaParaInserir: (Float, Float) -> Unit,
    aoSelecionarCampo: (String) -> Unit,
    aoAtualizarPosicaoCampo: (String, Float, Float) -> Unit,
    aoAlternarDimensaoCampo: (String) -> Unit,
    aoExcluirCampo: (String) -> Unit,
    aoIrPaginaAnterior: () -> Unit,
    aoIrProximaPagina: () -> Unit,
    aoVoltarPasso2: () -> Unit,
    aoAvancarPasso4: () -> Unit
) {
    val totalPaginas = uiState.arquivoPdfValidado?.totalPaginas ?: 1
    val signatarios = uiState.listaSignatariosSelecionadosOrdenada
    val signatarioAtivo = signatarios.firstOrNull {
        it.usuario.id == uiState.signatarioAtivoParaPosicionamentoId
    } ?: signatarios.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // 1. Cabeçalho Fixo e Compacto: Seleção de Signatários e Controles da Página
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .zIndex(2f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(signatarios, key = { _, item -> item.usuario.id }) { idx, item ->
                        val cor = obterCorSignatario(idx)
                        val selecionado = item.usuario.id == (signatarioAtivo?.usuario?.id)
                        val qtdCampos = uiState.contarCamposPorSignatario(item.usuario.id)

                        FilterChip(
                            selected = selecionado,
                            onClick = { aoSelecionarSignatarioAtivo(item.usuario.id) },
                            label = {
                                Text(
                                    text = "${item.usuario.nomeCompleto} ($qtdCampos)",
                                    fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                if (selecionado) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = cor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(cor)
                                    )
                                }
                            },
                            border = BorderStroke(
                                width = if (selecionado) 2.dp else 1.dp,
                                color = if (selecionado) cor else cor.copy(alpha = 0.45f)
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cor.copy(alpha = 0.18f),
                                selectedLabelColor = cor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = aoIrPaginaAnterior,
                            enabled = uiState.paginaAtualPdf > 0,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Página anterior"
                            )
                        }

                        val camposNaPagina = uiState.camposDaPaginaAtual.size
                        Text(
                            text = "Pág. ${uiState.paginaAtualPdf + 1}/$totalPaginas ($camposNaPagina campo${if (camposNaPagina == 1) "" else "s"})",
                            style = MaterialTheme.typography.labelLarge
                        )

                        IconButton(
                            onClick = aoIrProximaPagina,
                            enabled = uiState.paginaAtualPdf < totalPaginas - 1,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Próxima página"
                            )
                        }
                    }

                    Button(
                        onClick = aoAdicionarCampoNaPagina,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FiapPinkAccent),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddBox,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ Campo (${signatarioAtivo?.usuario?.nomeCompleto?.substringBefore(" ") ?: "Signatário"})",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.erroPasso3 != null,
            modifier = Modifier.zIndex(2f)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.erroPasso3.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Área Central do PDF: preenche o máximo de espaço possível dentro de weight(1f)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .zIndex(0f),
            contentAlignment = Alignment.Center
        ) {
            val paginaRenderizada = uiState.paginaRenderizada
            if (uiState.renderizandoPaginaPdf || paginaRenderizada == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Renderizando página ${uiState.paginaAtualPdf + 1} do PDF...")
                }
            } else {
                VisualizadorPaginaPdfDragDrop(
                    paginaRenderizada = paginaRenderizada,
                    camposDaPagina = uiState.camposDaPaginaAtual,
                    campoSelecionadoId = uiState.campoSelecionadoId,
                    aoSelecionarCampo = aoSelecionarCampo,
                    aoAtualizarPosicaoNormalizada = aoAtualizarPosicaoCampo,
                    aoAlternarDimensaoCampo = aoAlternarDimensaoCampo,
                    aoExcluirCampo = aoExcluirCampo,
                    aoTocarNaPaginaParaInserir = aoTocarNaPaginaParaInserir,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Rodapé Fixo: Navegação entre passos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .zIndex(2f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = aoVoltarPasso2,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Text("Voltar")
            }

            Button(
                onClick = aoAvancarPasso4,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LetsSignPurplePrimary),
                modifier = Modifier
                    .weight(1.6f)
                    .height(44.dp)
            ) {
                Text(
                    text = "Revisar (${uiState.camposPosicionados.size} campo${if (uiState.camposPosicionados.size == 1) "" else "s"})",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun ConteudoPasso4RevisaoEDisparo(
    uiState: CriacaoDocumentoUiState,
    aoAlterarMensagemConvite: (String) -> Unit,
    aoVoltarPasso3: () -> Unit,
    aoConfirmarEnvio: () -> Unit
) {
    val pdf = uiState.arquivoPdfValidado
    val signatarios = uiState.listaSignatariosSelecionadosOrdenada

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "4. Revisão Final e Disparo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Revise as informações do documento, os signatários e os campos demarcados antes de confirmar o envio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 1. Card de Resumo do Documento Final + Aviso de Imutabilidade
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = LetsSignPurplePrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.tituloDocumento,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Arquivo: ${pdf?.nomeArquivoOriginal ?: "-"} • ${pdf?.totalPaginas ?: 0} página(s)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = FiapPinkAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Regra de Imutabilidade: Após o envio, o documento e suas coordenadas não poderão ser editados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Card da Lista de Signatários e Campos de Assinatura Posicionados
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Signatários e Campos Posicionados (${signatarios.size} participante(s) • ${uiState.camposPosicionados.size} campo(s))",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    signatarios.forEachIndexed { idx, item ->
                        val cor = obterCorSignatario(idx)
                        val camposDoParticipante = uiState.listarCamposDoSignatario(item.usuario.id)

                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(cor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.usuario.nomeCompleto,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    color = cor.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = item.papel.descricaoExibicao,
                                        color = cor,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "${item.usuario.email} • ${item.usuario.telefone}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            camposDoParticipante.forEachIndexed { cIdx, campo ->
                                val descricaoCampo = if (camposDoParticipante.size == 1) {
                                    "Campo posicionado na Página ${campo.pagina + 1}"
                                } else {
                                    "Campo #${cIdx + 1} posicionado na Página ${campo.pagina + 1}"
                                }
                                Text(
                                    text = "• $descricaoCampo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = cor
                                )
                            }
                        }

                        if (idx < signatarios.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }

            // 3. Campo para Mensagem que acompanhará o Convite
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = null,
                            tint = LetsSignPurplePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mensagem do Convite",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.mensagemConvite,
                        onValueChange = aoAlterarMensagemConvite,
                        label = { Text("Mensagem enviada aos signatários") },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Alerta de erro no envio (permite nova tentativa sem duplicar solicitações)
            AnimatedVisibility(visible = uiState.erroPasso4 != null) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.erroPasso4.orEmpty()}\nVocê pode tentar enviar novamente com segurança sem duplicar solicitações.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Botões Voltar e Confirmar Envio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = aoVoltarPasso3,
                enabled = !uiState.enviandoDocumento,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("Voltar")
            }

            Button(
                onClick = aoConfirmarEnvio,
                enabled = !uiState.enviandoDocumento,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FiapPinkAccent),
                modifier = Modifier
                    .weight(1.8f)
                    .height(52.dp)
            ) {
                if (uiState.enviandoDocumento) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviando...")
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar Envio", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DialogSucessoDisparoDocumento(
    documento: Documento,
    totalSignatarios: Int,
    totalCampos: Int,
    aoVoltarAoDashboard: () -> Unit,
    aoAcompanharDocumento: () -> Unit
) {
    AlertDialog(
        onDismissRequest = aoVoltarAoDashboard,
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
                text = "Documento Enviado com Sucesso!",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "O documento '${documento.titulo}' foi registrado e enviado para $totalSignatarios signatário(s) com $totalCampos campo(s) de assinatura demarcado(s).",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Status atual: Enviado (Imutável • v${documento.versao})",
                    style = MaterialTheme.typography.labelLarge,
                    color = LetsSignPurplePrimary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = aoAcompanharDocumento,
                colors = ButtonDefaults.buttonColors(containerColor = LetsSignPurplePrimary)
            ) {
                Text("Acompanhar Documento")
            }
        },
        dismissButton = {
            TextButton(onClick = aoVoltarAoDashboard) {
                Text("Voltar ao Dashboard")
            }
        }
    )
}

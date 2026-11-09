package br.com.fiap.letssign.presentation.signatario.assinatura

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.core.pdf.RenderizadorPdfHelper
import br.com.fiap.letssign.domain.usecase.documento.AssinarDocumentoUseCase
import br.com.fiap.letssign.domain.usecase.documento.ObterDetalhesAcompanhamentoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssinaturaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val obterDetalhesAcompanhamentoUseCase: ObterDetalhesAcompanhamentoUseCase,
    private val assinarDocumentoUseCase: AssinarDocumentoUseCase,
    private val renderizadorPdfHelper: RenderizadorPdfHelper
) : ViewModel() {

    private val documentoId: String = checkNotNull(savedStateHandle["documentoId"])
    private val signatarioId: String = checkNotNull(savedStateHandle["signatarioId"])

    private val _uiState = MutableStateFlow(AssinaturaUiState())
    val uiState: StateFlow<AssinaturaUiState> = _uiState.asStateFlow()

    private var focoInicialRealizado = false

    init {
        observarDocumentoECamposDoSignatario()
    }

    private fun observarDocumentoECamposDoSignatario() {
        obterDetalhesAcompanhamentoUseCase(documentoId)
            .onEach { detalhes ->
                if (detalhes == null) {
                    _uiState.update {
                        it.copy(
                            carregandoDados = false,
                            erro = "Documento não encontrado."
                        )
                    }
                    return@onEach
                }

                val participacao = detalhes.signatarios.firstOrNull {
                    it.signatarioId == signatarioId
                }
                val camposProprios = if (participacao != null) {
                    detalhes.camposAssinatura
                        .filter { it.signatarioDocumentoId == participacao.id }
                        .sortedBy { it.pagina }
                } else {
                    emptyList()
                }

                // Ao abrir pela primeira vez, foca automaticamente na página onde o campo do signatário está posicionado
                val paginaAlvo = if (!focoInicialRealizado && camposProprios.isNotEmpty()) {
                    focoInicialRealizado = true
                    camposProprios.first().pagina
                } else {
                    _uiState.value.paginaAtualPdf
                }

                _uiState.update {
                    it.copy(
                        detalhes = detalhes,
                        participacaoSignatario = participacao,
                        camposDoSignatario = camposProprios,
                        paginaAtualPdf = paginaAlvo,
                        carregandoDados = false
                    )
                }

                renderizarPaginaDoPdf(paginaAlvo, detalhes.documento.caminhoPdfAtual)
            }
            .launchIn(viewModelScope)
    }

    private fun renderizarPaginaDoPdf(indicePagina: Int, caminhoPdf: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(renderizandoPdf = true) }
            val resultado = renderizadorPdfHelper.renderizarPagina(
                caminhoArquivoPdf = caminhoPdf,
                indicePagina = indicePagina
            )
            resultado.fold(
                onSuccess = { pagina ->
                    _uiState.update {
                        it.copy(
                            paginaAtualPdf = pagina.indicePagina,
                            paginaRenderizada = pagina,
                            renderizandoPdf = false
                        )
                    }
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            renderizandoPdf = false,
                            erro = "Erro ao renderizar PDF: ${erro.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun focarNoProximoCampoDeAssinatura() {
        val estado = _uiState.value
        val campos = estado.camposDoSignatario
        if (campos.isEmpty()) return

        val proximoIndice = (estado.indiceCampoFocado + 1) % campos.size
        val campoAlvo = campos[proximoIndice]

        _uiState.update {
            it.copy(
                indiceCampoFocado = proximoIndice,
                erro = null
            )
        }

        val caminhoPdf = estado.detalhes?.documento?.caminhoPdfAtual ?: return
        if (campoAlvo.pagina != estado.paginaAtualPdf) {
            renderizarPaginaDoPdf(campoAlvo.pagina, caminhoPdf)
        }
    }

    fun irParaPaginaAnterior() {
        val estado = _uiState.value
        val caminhoPdf = estado.detalhes?.documento?.caminhoPdfAtual ?: return
        if (estado.paginaAtualPdf > 0) {
            renderizarPaginaDoPdf(estado.paginaAtualPdf - 1, caminhoPdf)
        }
    }

    fun irParaProximaPagina() {
        val estado = _uiState.value
        val total = estado.detalhes?.documento?.totalPaginas ?: 1
        val caminhoPdf = estado.detalhes?.documento?.caminhoPdfAtual ?: return
        if (estado.paginaAtualPdf < total - 1) {
            renderizarPaginaDoPdf(estado.paginaAtualPdf + 1, caminhoPdf)
        }
    }

    fun abrirQuadroRubrica() {
        if (_uiState.value.jaAssinadoPorEsteSignatario) return
        _uiState.update { it.copy(exibirQuadroRubricaDialog = true, erro = null) }
    }

    fun fecharQuadroRubrica() {
        _uiState.update { it.copy(exibirQuadroRubricaDialog = false) }
    }

    fun confirmarAssinaturaNoPdf(bitmapRubrica: Bitmap) {
        val estado = _uiState.value
        if (estado.processandoAssinaturaPdfBox) return

        val detalhes = estado.detalhes ?: return
        val participacao = estado.participacaoSignatario ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    exibirQuadroRubricaDialog = false,
                    processandoAssinaturaPdfBox = true,
                    erro = null
                )
            }

            val resultado = assinarDocumentoUseCase(
                documentoId = detalhes.documento.id,
                signatarioId = signatarioId,
                nomeSignatario = participacao.nomeSignatario,
                papelSignatario = participacao.papel.descricaoExibicao,
                caminhoPdfAtual = detalhes.documento.caminhoPdfAtual,
                camposDoSignatario = estado.camposDoSignatario,
                bitmapAssinatura = bitmapRubrica
            )

            when (resultado) {
                is ResultadoOperacao.Sucesso -> {
                    val docAtualizado = resultado.dados
                    // Re-renderiza a página atual já com a assinatura estampada pelo PDFBox
                    renderizarPaginaDoPdf(estado.paginaAtualPdf, docAtualizado.caminhoPdfAtual)
                    _uiState.update {
                        it.copy(
                            processandoAssinaturaPdfBox = false,
                            documentoAssinadoComSucesso = docAtualizado,
                            erro = null
                        )
                    }
                }
                is ResultadoOperacao.Erro -> {
                    _uiState.update {
                        it.copy(
                            processandoAssinaturaPdfBox = false,
                            erro = resultado.mensagem
                        )
                    }
                }
            }
        }
    }
}

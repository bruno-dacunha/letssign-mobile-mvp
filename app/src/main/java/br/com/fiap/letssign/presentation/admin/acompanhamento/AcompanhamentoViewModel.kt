package br.com.fiap.letssign.presentation.admin.acompanhamento

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.letssign.core.pdf.RenderizadorPdfHelper
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
class AcompanhamentoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val obterDetalhesAcompanhamentoUseCase: ObterDetalhesAcompanhamentoUseCase,
    private val renderizadorPdfHelper: RenderizadorPdfHelper
) : ViewModel() {

    private val documentoId: String = checkNotNull(savedStateHandle["documentoId"])

    private val _uiState = MutableStateFlow(AcompanhamentoUiState())
    val uiState: StateFlow<AcompanhamentoUiState> = _uiState.asStateFlow()

    init {
        observarDetalhesDocumento()
    }

    private fun observarDetalhesDocumento() {
        obterDetalhesAcompanhamentoUseCase(documentoId)
            .onEach { detalhes ->
                _uiState.update {
                    it.copy(
                        detalhes = detalhes,
                        carregando = false
                    )
                }
                if (detalhes != null && _uiState.value.paginaRenderizada == null) {
                    renderizarPaginaPdf(0, detalhes.documento.caminhoPdfAtual)
                }
            }
            .launchIn(viewModelScope)
    }

    fun selecionarAba(aba: AbaAcompanhamento) {
        _uiState.update { it.copy(abaSelecionada = aba) }
        if (aba == AbaAcompanhamento.VISUALIZAR_PDF) {
            val caminho = _uiState.value.detalhes?.documento?.caminhoPdfAtual ?: return
            renderizarPaginaPdf(_uiState.value.paginaAtualPdf, caminho)
        }
    }

    fun irParaPaginaAnterior() {
        val atual = _uiState.value.paginaAtualPdf
        val caminho = _uiState.value.detalhes?.documento?.caminhoPdfAtual ?: return
        if (atual > 0) {
            renderizarPaginaPdf(atual - 1, caminho)
        }
    }

    fun irParaProximaPagina() {
        val estado = _uiState.value
        val total = estado.detalhes?.documento?.totalPaginas ?: 1
        val caminho = estado.detalhes?.documento?.caminhoPdfAtual ?: return
        if (estado.paginaAtualPdf < total - 1) {
            renderizarPaginaPdf(estado.paginaAtualPdf + 1, caminho)
        }
    }

    private fun renderizarPaginaPdf(indicePagina: Int, caminhoPdf: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(renderizandoPdf = true, erroPdf = null) }
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
                            erroPdf = "Não foi possível renderizar o PDF: ${erro.localizedMessage}"
                        )
                    }
                }
            )
        }
    }
}

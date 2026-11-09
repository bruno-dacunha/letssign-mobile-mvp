package br.com.fiap.letssign.presentation.admin.criacao

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.core.pdf.RenderizadorPdfHelper
import br.com.fiap.letssign.domain.model.PapelSignatario
import br.com.fiap.letssign.domain.model.Usuario
import br.com.fiap.letssign.domain.usecase.documento.ConfirmarEnvioDocumentoUseCase
import br.com.fiap.letssign.domain.usecase.documento.ImportarEValidarPdfUseCase
import br.com.fiap.letssign.domain.usecase.documento.ValidarPasso1UploadUseCase
import br.com.fiap.letssign.domain.usecase.documento.ValidarPasso2SignatariosUseCase
import br.com.fiap.letssign.domain.usecase.documento.ValidarPasso3PosicionamentoUseCase
import br.com.fiap.letssign.domain.usecase.signatario.ListarSignatariosDoAdminUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CriacaoDocumentoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val importarEValidarPdfUseCase: ImportarEValidarPdfUseCase,
    private val validarPasso1UploadUseCase: ValidarPasso1UploadUseCase,
    private val validarPasso2SignatariosUseCase: ValidarPasso2SignatariosUseCase,
    private val validarPasso3PosicionamentoUseCase: ValidarPasso3PosicionamentoUseCase,
    private val confirmarEnvioDocumentoUseCase: ConfirmarEnvioDocumentoUseCase,
    private val listarSignatariosDoAdminUseCase: ListarSignatariosDoAdminUseCase,
    private val renderizadorPdfHelper: RenderizadorPdfHelper
) : ViewModel() {

    val adminId: String = checkNotNull(savedStateHandle["adminId"])

    private val _uiState = MutableStateFlow(CriacaoDocumentoUiState())
    val uiState: StateFlow<CriacaoDocumentoUiState> = _uiState.asStateFlow()

    init {
        observarSignatariosCadastrados()
    }

    private fun observarSignatariosCadastrados() {
        listarSignatariosDoAdminUseCase(adminId)
            .onEach { lista ->
                _uiState.update { estadoAtual ->
                    val selecionadosAtualizados = estadoAtual.signatariosSelecionados
                        .mapNotNull { (id, selecao) ->
                            val usuarioAtualizado = lista.firstOrNull { it.id == id }
                            usuarioAtualizado?.let { id to selecao.copy(usuario = it) }
                        }
                        .toMap()

                    estadoAtual.copy(
                        signatariosCadastrados = lista,
                        signatariosSelecionados = selecionadosAtualizados,
                        carregandoSignatarios = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    // Passo 1: Upload e validação do PDF

    fun aoAlterarTituloDocumento(novoTitulo: String) {
        _uiState.update {
            it.copy(
                tituloDocumento = novoTitulo,
                erroPasso1 = null
            )
        }
    }

    fun aoSelecionarArquivoPdfNoCelular(uriString: String?) {
        if (uriString.isNullOrBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processandoPdf = true,
                    erroPasso1 = null
                )
            }

            when (val resultado = importarEValidarPdfUseCase(uriString)) {
                is ResultadoOperacao.Sucesso -> {
                    val pdfValidado = resultado.dados
                    _uiState.update { estado ->
                        val tituloSugerido = if (estado.tituloDocumento.isBlank()) {
                            pdfValidado.nomeArquivoOriginal
                                .removeSuffix(".pdf")
                                .removeSuffix(".PDF")
                                .replace("_", " ")
                        } else {
                            estado.tituloDocumento
                        }
                        estado.copy(
                            processandoPdf = false,
                            arquivoPdfValidado = pdfValidado,
                            tituloDocumento = tituloSugerido,
                            paginaAtualPdf = 0,
                            paginaRenderizada = null,
                            camposPosicionados = emptyList(),
                            erroPasso1 = null
                        )
                    }
                }
                is ResultadoOperacao.Erro -> {
                    _uiState.update {
                        it.copy(
                            processandoPdf = false,
                            arquivoPdfValidado = null,
                            erroPasso1 = resultado.mensagem
                        )
                    }
                }
            }
        }
    }

    fun gerarPdfModeloParaTeste() {
        viewModelScope.launch {
            _uiState.update { it.copy(processandoPdf = true, erroPasso1 = null) }

            val tituloBase = _uiState.value.tituloDocumento.ifBlank {
                "Contrato de Prestação de Serviços"
            }

            when (val resultado = importarEValidarPdfUseCase.gerarContratoDemonstracao(tituloBase)) {
                is ResultadoOperacao.Sucesso -> {
                    _uiState.update {
                        it.copy(
                            processandoPdf = false,
                            tituloDocumento = tituloBase,
                            arquivoPdfValidado = resultado.dados,
                            paginaAtualPdf = 0,
                            paginaRenderizada = null,
                            camposPosicionados = emptyList(),
                            erroPasso1 = null
                        )
                    }
                }
                is ResultadoOperacao.Erro -> {
                    _uiState.update {
                        it.copy(
                            processandoPdf = false,
                            erroPasso1 = resultado.mensagem
                        )
                    }
                }
            }
        }
    }

    fun removerPdfSelecionado() {
        _uiState.update {
            it.copy(
                arquivoPdfValidado = null,
                paginaAtualPdf = 0,
                paginaRenderizada = null,
                camposPosicionados = emptyList(),
                erroPasso1 = null
            )
        }
    }

    fun avancarDoPasso1ParaPasso2() {
        val estado = _uiState.value
        val validacao = validarPasso1UploadUseCase(
            tituloDocumento = estado.tituloDocumento,
            arquivoPdf = estado.arquivoPdfValidado
        )

        when (validacao) {
            is ResultadoOperacao.Sucesso -> {
                _uiState.update {
                    it.copy(
                        etapaAtual = EtapaWizard.PASSO_2_SIGNATARIOS,
                        erroPasso1 = null
                    )
                }
            }
            is ResultadoOperacao.Erro -> {
                _uiState.update { it.copy(erroPasso1 = validacao.mensagem) }
            }
        }
    }

    // Passo 2: Seleção e classificação de signatários

    fun aoAlterarBuscaSignatario(termo: String) {
        _uiState.update { it.copy(termoBuscaSignatario = termo) }
    }

    fun alternarSelecaoSignatario(usuario: Usuario) {
        _uiState.update { estado ->
            val mapaAtual = estado.signatariosSelecionados.toMutableMap()
            if (mapaAtual.containsKey(usuario.id)) {
                mapaAtual.remove(usuario.id)
            } else {
                mapaAtual[usuario.id] = SignatarioSelecionadoUi(
                    usuario = usuario,
                    papel = PapelSignatario.CONTRATANTE,
                    obrigatorio = true
                )
            }
            val camposFiltrados = estado.camposPosicionados.filter {
                mapaAtual.containsKey(it.signatarioId)
            }
            estado.copy(
                signatariosSelecionados = mapaAtual,
                camposPosicionados = camposFiltrados,
                erroPasso2 = null
            )
        }
    }

    fun alterarPapelDoSignatario(signatarioId: String, novoPapel: PapelSignatario) {
        _uiState.update { estado ->
            val itemAtual = estado.signatariosSelecionados[signatarioId] ?: return@update estado
            val mapaAtualizado = estado.signatariosSelecionados.toMutableMap().apply {
                put(signatarioId, itemAtual.copy(papel = novoPapel))
            }
            val camposAtualizados = estado.camposPosicionados.map { campo ->
                if (campo.signatarioId == signatarioId) {
                    campo.copy(papelSignatario = novoPapel)
                } else {
                    campo
                }
            }
            estado.copy(
                signatariosSelecionados = mapaAtualizado,
                camposPosicionados = camposAtualizados,
                erroPasso2 = null
            )
        }
    }

    fun alternarObrigatoriedadeSignatario(signatarioId: String) {
        _uiState.update { estado ->
            val itemAtual = estado.signatariosSelecionados[signatarioId] ?: return@update estado
            val mapaAtualizado = estado.signatariosSelecionados.toMutableMap().apply {
                put(signatarioId, itemAtual.copy(obrigatorio = !itemAtual.obrigatorio))
            }
            estado.copy(
                signatariosSelecionados = mapaAtualizado,
                erroPasso2 = null
            )
        }
    }

    fun removerSignatarioSelecionado(signatarioId: String) {
        _uiState.update { estado ->
            val mapaAtualizado = estado.signatariosSelecionados.toMutableMap().apply {
                remove(signatarioId)
            }
            val camposFiltrados = estado.camposPosicionados.filter { it.signatarioId != signatarioId }
            estado.copy(
                signatariosSelecionados = mapaAtualizado,
                camposPosicionados = camposFiltrados,
                erroPasso2 = null
            )
        }
    }

    fun avancarDoPasso2ParaPasso3() {
        val estado = _uiState.value
        val validacao = validarPasso2SignatariosUseCase(
            signatariosSelecionados = estado.converterSelecaoParaInputs()
        )

        when (validacao) {
            is ResultadoOperacao.Sucesso -> {
                val primeiroSignatarioId = estado.signatariosSelecionados.keys.firstOrNull()
                val signatarioAtivo = if (
                    estado.signatarioAtivoParaPosicionamentoId != null &&
                    estado.signatariosSelecionados.containsKey(estado.signatarioAtivoParaPosicionamentoId)
                ) {
                    estado.signatarioAtivoParaPosicionamentoId
                } else {
                    primeiroSignatarioId
                }

                _uiState.update {
                    it.copy(
                        etapaAtual = EtapaWizard.PASSO_3_POSICIONAMENTO,
                        signatarioAtivoParaPosicionamentoId = signatarioAtivo,
                        erroPasso2 = null,
                        erroPasso3 = null
                    )
                }
                renderizarPaginaDoPdf(_uiState.value.paginaAtualPdf)
            }
            is ResultadoOperacao.Erro -> {
                _uiState.update { it.copy(erroPasso2 = validacao.mensagem) }
            }
        }
    }

    // Passo 3: Renderização do PDF e posicionamento Drag & Drop

    private fun renderizarPaginaDoPdf(indicePagina: Int) {
        val caminhoPdf = _uiState.value.arquivoPdfValidado?.caminhoLocalPrivado ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(renderizandoPaginaPdf = true, erroPasso3 = null) }

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
                            renderizandoPaginaPdf = false
                        )
                    }
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            renderizandoPaginaPdf = false,
                            erroPasso3 = "Erro ao renderizar página do PDF: ${erro.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun irParaPaginaAnteriorPdf() {
        val atual = _uiState.value.paginaAtualPdf
        if (atual > 0) {
            renderizarPaginaDoPdf(atual - 1)
        }
    }

    fun irParaProximaPaginaPdf() {
        val estado = _uiState.value
        val total = estado.arquivoPdfValidado?.totalPaginas ?: 1
        if (estado.paginaAtualPdf < total - 1) {
            renderizarPaginaDoPdf(estado.paginaAtualPdf + 1)
        }
    }

    fun selecionarSignatarioAtivoParaPosicionamento(signatarioId: String) {
        if (_uiState.value.signatariosSelecionados.containsKey(signatarioId)) {
            _uiState.update {
                it.copy(
                    signatarioAtivoParaPosicionamentoId = signatarioId,
                    erroPasso3 = null
                )
            }
        }
    }

    fun selecionarCampoPosicionado(campoId: String) {
        _uiState.update { it.copy(campoSelecionadoId = campoId) }
    }

    fun adicionarCampoAssinaturaNaPaginaAtual(
        xRelativoToque: Float? = null,
        yRelativoToque: Float? = null
    ) {
        val estado = _uiState.value
        val signatarioId = estado.signatarioAtivoParaPosicionamentoId
            ?: estado.signatariosSelecionados.keys.firstOrNull()
            ?: return

        val signatarioConfig = estado.signatariosSelecionados[signatarioId] ?: return
        val indiceCor = estado.listaSignatariosSelecionadosOrdenada
            .indexOfFirst { it.usuario.id == signatarioId }
            .coerceAtLeast(0)

        val larguraPadrao = CampoAssinaturaPosicionadoUi.LARGURA_PADRAO_RELATIVA
        val alturaPadrao = CampoAssinaturaPosicionadoUi.ALTURA_PADRAO_RELATIVA

        val qtdExistentesNaPagina = estado.camposDaPaginaAtual.size
        val deslocamentoCascata = (qtdExistentesNaPagina % 5) * 0.045f

        val xInicial = if (xRelativoToque != null) {
            (xRelativoToque - larguraPadrao / 2f).coerceIn(0f, 1f - larguraPadrao)
        } else {
            (0.12f + deslocamentoCascata).coerceIn(0f, 1f - larguraPadrao)
        }

        val yInicial = if (yRelativoToque != null) {
            (yRelativoToque - alturaPadrao / 2f).coerceIn(0f, 1f - alturaPadrao)
        } else {
            (0.62f + deslocamentoCascata).coerceIn(0f, 1f - alturaPadrao)
        }

        val novoCampo = CampoAssinaturaPosicionadoUi(
            id = UUID.randomUUID().toString(),
            signatarioId = signatarioId,
            nomeSignatario = signatarioConfig.usuario.nomeCompleto,
            papelSignatario = signatarioConfig.papel,
            pagina = estado.paginaAtualPdf,
            posicaoXRelativa = xInicial,
            posicaoYRelativa = yInicial,
            larguraRelativa = larguraPadrao,
            alturaRelativa = alturaPadrao,
            indiceCor = indiceCor
        )

        _uiState.update {
            it.copy(
                camposPosicionados = it.camposPosicionados + novoCampo,
                campoSelecionadoId = novoCampo.id,
                erroPasso3 = null
            )
        }
    }

    fun atualizarPosicaoCampoNormalizada(
        campoId: String,
        novoXRelativo: Float,
        novoYRelativo: Float
    ) {
        _uiState.update { estado ->
            val listaAtualizada = estado.camposPosicionados.map { campo ->
                if (campo.id == campoId) {
                    val maxX = (1f - campo.larguraRelativa).coerceAtLeast(0f)
                    val maxY = (1f - campo.alturaRelativa).coerceAtLeast(0f)
                    campo.copy(
                        posicaoXRelativa = novoXRelativo.coerceIn(0f, maxX),
                        posicaoYRelativa = novoYRelativo.coerceIn(0f, maxY)
                    )
                } else {
                    campo
                }
            }
            estado.copy(
                camposPosicionados = listaAtualizada,
                campoSelecionadoId = campoId,
                erroPasso3 = null
            )
        }
    }

    fun alternarDimensaoCampo(campoId: String) {
        _uiState.update { estado ->
            val listaAtualizada = estado.camposPosicionados.map { campo ->
                if (campo.id == campoId) {
                    val (novaLargura, novaAltura) = when {
                        campo.larguraRelativa < 0.33f -> 0.38f to 0.10f
                        campo.larguraRelativa < 0.43f -> 0.48f to 0.12f
                        else -> 0.30f to 0.085f
                    }
                    val xAjustado = campo.posicaoXRelativa.coerceIn(0f, (1f - novaLargura).coerceAtLeast(0f))
                    val yAjustado = campo.posicaoYRelativa.coerceIn(0f, (1f - novaAltura).coerceAtLeast(0f))
                    campo.copy(
                        posicaoXRelativa = xAjustado,
                        posicaoYRelativa = yAjustado,
                        larguraRelativa = novaLargura,
                        alturaRelativa = novaAltura
                    )
                } else {
                    campo
                }
            }
            estado.copy(camposPosicionados = listaAtualizada)
        }
    }

    fun excluirCampoAssinatura(campoId: String) {
        _uiState.update { estado ->
            estado.copy(
                camposPosicionados = estado.camposPosicionados.filterNot { it.id == campoId },
                campoSelecionadoId = if (estado.campoSelecionadoId == campoId) null else estado.campoSelecionadoId,
                erroPasso3 = null
            )
        }
    }

    fun avancarDoPasso3ParaPasso4() {
        val estado = _uiState.value
        val totalPaginas = estado.arquivoPdfValidado?.totalPaginas ?: 1
        val mapaNomes = estado.signatariosSelecionados.mapValues { it.value.usuario.nomeCompleto }

        val validacao = validarPasso3PosicionamentoUseCase(
            totalPaginasPdf = totalPaginas,
            signatariosSelecionados = estado.converterSelecaoParaInputs(),
            camposPosicionados = estado.converterCamposParaInputs(),
            nomesPorSignatarioId = mapaNomes
        )

        when (validacao) {
            is ResultadoOperacao.Sucesso -> {
                _uiState.update {
                    it.copy(
                        etapaAtual = EtapaWizard.PASSO_4_REVISAO,
                        erroPasso3 = null,
                        erroPasso4 = null
                    )
                }
            }
            is ResultadoOperacao.Erro -> {
                _uiState.update { it.copy(erroPasso3 = validacao.mensagem) }
            }
        }
    }

    // Passo 4: Revisão e disparo do documento

    fun aoAlterarMensagemConvite(novaMensagem: String) {
        _uiState.update {
            it.copy(
                mensagemConvite = novaMensagem,
                erroPasso4 = null
            )
        }
    }

    fun confirmarEDispararDocumento() {
        val estado = _uiState.value
        if (estado.enviandoDocumento) return

        viewModelScope.launch {
            _uiState.update { it.copy(enviandoDocumento = true, erroPasso4 = null) }

            val mapaNomes = estado.signatariosSelecionados.mapValues { it.value.usuario.nomeCompleto }
            val resultado = confirmarEnvioDocumentoUseCase(
                adminId = adminId,
                tituloDocumento = estado.tituloDocumento,
                arquivoPdf = estado.arquivoPdfValidado,
                mensagemConvite = estado.mensagemConvite,
                signatariosSelecionados = estado.converterSelecaoParaInputs(),
                camposPosicionados = estado.converterCamposParaInputs(),
                nomesPorSignatarioId = mapaNomes,
                documentoIdTentativa = estado.documentoIdTentativa
            )

            when (resultado) {
                is ResultadoOperacao.Sucesso -> {
                    _uiState.update {
                        it.copy(
                            enviandoDocumento = false,
                            documentoEnviadoComSucesso = resultado.dados,
                            erroPasso4 = null
                        )
                    }
                }
                is ResultadoOperacao.Erro -> {
                    _uiState.update {
                        it.copy(
                            enviandoDocumento = false,
                            erroPasso4 = resultado.mensagem
                        )
                    }
                }
            }
        }
    }

    fun voltarEtapaAnterior(aoSairDoWizard: () -> Unit) {
        when (_uiState.value.etapaAtual) {
            EtapaWizard.PASSO_1_UPLOAD -> aoSairDoWizard()
            EtapaWizard.PASSO_2_SIGNATARIOS -> {
                _uiState.update { it.copy(etapaAtual = EtapaWizard.PASSO_1_UPLOAD, erroPasso2 = null) }
            }
            EtapaWizard.PASSO_3_POSICIONAMENTO -> {
                _uiState.update { it.copy(etapaAtual = EtapaWizard.PASSO_2_SIGNATARIOS, erroPasso3 = null) }
            }
            EtapaWizard.PASSO_4_REVISAO -> {
                _uiState.update { it.copy(etapaAtual = EtapaWizard.PASSO_3_POSICIONAMENTO, erroPasso4 = null) }
            }
        }
    }
}

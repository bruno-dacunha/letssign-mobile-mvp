package br.com.fiap.letssign.presentation.admin.criacao

import br.com.fiap.letssign.core.pdf.PaginaPdfRenderizada
import br.com.fiap.letssign.domain.model.ArquivoPdfValidado
import br.com.fiap.letssign.domain.model.Documento
import br.com.fiap.letssign.domain.model.PapelSignatario
import br.com.fiap.letssign.domain.model.Usuario
import br.com.fiap.letssign.domain.repository.PosicaoCampoInput
import br.com.fiap.letssign.domain.repository.SelecaoSignatarioInput
import java.util.UUID

enum class EtapaWizard(val numero: Int, val tituloCurto: String) {
    PASSO_1_UPLOAD(1, "Upload PDF"),
    PASSO_2_SIGNATARIOS(2, "Signatários"),
    PASSO_3_POSICIONAMENTO(3, "Posicionar"),
    PASSO_4_REVISAO(4, "Revisão")
}

data class SignatarioSelecionadoUi(
    val usuario: Usuario,
    val papel: PapelSignatario = PapelSignatario.CONTRATANTE,
    val obrigatorio: Boolean = true
)

// Coordenadas (X, Y) e dimensões normalizadas (0.0f a 1.0f) em relação à página do PDF
data class CampoAssinaturaPosicionadoUi(
    val id: String,
    val signatarioId: String,
    val nomeSignatario: String,
    val papelSignatario: PapelSignatario,
    val pagina: Int,
    val posicaoXRelativa: Float,
    val posicaoYRelativa: Float,
    val larguraRelativa: Float = LARGURA_PADRAO_RELATIVA,
    val alturaRelativa: Float = ALTURA_PADRAO_RELATIVA,
    val indiceCor: Int = 0
) {
    companion object {
        const val LARGURA_PADRAO_RELATIVA = 0.38f
        const val ALTURA_PADRAO_RELATIVA = 0.10f
    }
}

data class CriacaoDocumentoUiState(
    val etapaAtual: EtapaWizard = EtapaWizard.PASSO_1_UPLOAD,
    val documentoIdTentativa: String = UUID.randomUUID().toString(),

    // Passo 1
    val tituloDocumento: String = "",
    val arquivoPdfValidado: ArquivoPdfValidado? = null,
    val processandoPdf: Boolean = false,
    val erroPasso1: String? = null,

    // Passo 2
    val signatariosCadastrados: List<Usuario> = emptyList(),
    val carregandoSignatarios: Boolean = true,
    val termoBuscaSignatario: String = "",
    val signatariosSelecionados: Map<String, SignatarioSelecionadoUi> = emptyMap(),
    val erroPasso2: String? = null,

    // Passo 3
    val paginaAtualPdf: Int = 0,
    val paginaRenderizada: PaginaPdfRenderizada? = null,
    val renderizandoPaginaPdf: Boolean = false,
    val signatarioAtivoParaPosicionamentoId: String? = null,
    val campoSelecionadoId: String? = null,
    val camposPosicionados: List<CampoAssinaturaPosicionadoUi> = emptyList(),
    val erroPasso3: String? = null,

    // Passo 4
    val mensagemConvite: String = "Olá! Por favor, revise e assine digitalmente este documento pela plataforma LetsSign.",
    val enviandoDocumento: Boolean = false,
    val erroPasso4: String? = null,
    val documentoEnviadoComSucesso: Documento? = null
) {
    val signatariosFiltradosPasso2: List<Usuario>
        get() = if (termoBuscaSignatario.isBlank()) {
            signatariosCadastrados
        } else {
            val termo = termoBuscaSignatario.trim().lowercase()
            signatariosCadastrados.filter {
                it.nomeCompleto.lowercase().contains(termo) ||
                    it.email.lowercase().contains(termo)
            }
        }

    val listaSignatariosSelecionadosOrdenada: List<SignatarioSelecionadoUi>
        get() = signatariosSelecionados.values.toList()

    val camposDaPaginaAtual: List<CampoAssinaturaPosicionadoUi>
        get() = camposPosicionados.filter { it.pagina == paginaAtualPdf }

    fun contarCamposPorSignatario(signatarioId: String): Int =
        camposPosicionados.count { it.signatarioId == signatarioId }

    fun listarCamposDoSignatario(signatarioId: String): List<CampoAssinaturaPosicionadoUi> =
        camposPosicionados.filter { it.signatarioId == signatarioId }

    fun converterSelecaoParaInputs(): List<SelecaoSignatarioInput> =
        signatariosSelecionados.values.map { item ->
            SelecaoSignatarioInput(
                signatarioId = item.usuario.id,
                papel = item.papel,
                obrigatorio = item.obrigatorio
            )
        }

    fun converterCamposParaInputs(): List<PosicaoCampoInput> =
        camposPosicionados.map { campo ->
            PosicaoCampoInput(
                id = campo.id,
                signatarioId = campo.signatarioId,
                pagina = campo.pagina,
                posicaoXRelativa = campo.posicaoXRelativa,
                posicaoYRelativa = campo.posicaoYRelativa,
                larguraRelativa = campo.larguraRelativa,
                alturaRelativa = campo.alturaRelativa
            )
        }
}

package br.com.fiap.letssign.presentation.signatario.assinatura

import br.com.fiap.letssign.core.pdf.PaginaPdfRenderizada
import br.com.fiap.letssign.domain.model.CampoAssinatura
import br.com.fiap.letssign.domain.model.DetalhesAcompanhamentoDocumento
import br.com.fiap.letssign.domain.model.Documento
import br.com.fiap.letssign.domain.model.SignatarioDocumento
import br.com.fiap.letssign.domain.model.StatusAssinatura

data class AssinaturaUiState(
    val detalhes: DetalhesAcompanhamentoDocumento? = null,
    val participacaoSignatario: SignatarioDocumento? = null,
    val camposDoSignatario: List<CampoAssinatura> = emptyList(),
    val indiceCampoFocado: Int = 0,
    val paginaAtualPdf: Int = 0,
    val paginaRenderizada: PaginaPdfRenderizada? = null,
    val carregandoDados: Boolean = true,
    val renderizandoPdf: Boolean = false,
    val exibirQuadroRubricaDialog: Boolean = false,
    val processandoAssinaturaPdfBox: Boolean = false,
    val erro: String? = null,
    val documentoAssinadoComSucesso: Documento? = null
) {
    val jaAssinadoPorEsteSignatario: Boolean
        get() = participacaoSignatario?.status == StatusAssinatura.ASSINADO

    val campoAtualmenteFocado: CampoAssinatura?
        get() = camposDoSignatario.getOrNull(indiceCampoFocado)

    val camposDoSignatarioNaPaginaAtual: List<CampoAssinatura>
        get() = camposDoSignatario.filter { it.pagina == paginaAtualPdf }
}

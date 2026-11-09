package br.com.fiap.letssign.presentation.admin.acompanhamento

import br.com.fiap.letssign.core.pdf.PaginaPdfRenderizada
import br.com.fiap.letssign.domain.model.DetalhesAcompanhamentoDocumento

enum class AbaAcompanhamento(val titulo: String) {
    RESUMO_E_AUDITORIA("Signatários e Histórico"),
    VISUALIZAR_PDF("Documento PDF")
}

data class AcompanhamentoUiState(
    val detalhes: DetalhesAcompanhamentoDocumento? = null,
    val carregando: Boolean = true,
    val abaSelecionada: AbaAcompanhamento = AbaAcompanhamento.RESUMO_E_AUDITORIA,
    val paginaAtualPdf: Int = 0,
    val paginaRenderizada: PaginaPdfRenderizada? = null,
    val renderizandoPdf: Boolean = false,
    val erroPdf: String? = null
)

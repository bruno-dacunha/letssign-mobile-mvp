package br.com.fiap.letssign.presentation.admin.dashboard

import br.com.fiap.letssign.domain.model.ResumoDocumentoAdmin
import br.com.fiap.letssign.domain.model.StatusDocumento

data class DashboardAdminUiState(
    val todosDocumentos: List<ResumoDocumentoAdmin> = emptyList(),
    val abaSelecionada: StatusDocumento = StatusDocumento.ENVIADO,
    val carregando: Boolean = true
) {
    val documentosEnviados: List<ResumoDocumentoAdmin>
        get() = todosDocumentos.filter { it.documento.status == StatusDocumento.ENVIADO }

    val documentosParcialmenteAssinados: List<ResumoDocumentoAdmin>
        get() = todosDocumentos.filter { it.documento.status == StatusDocumento.PARCIALMENTE_ASSINADO }

    val documentosConcluidos: List<ResumoDocumentoAdmin>
        get() = todosDocumentos.filter { it.documento.status == StatusDocumento.CONCLUIDO }

    val documentosDaAbaAtual: List<ResumoDocumentoAdmin>
        get() = when (abaSelecionada) {
            StatusDocumento.ENVIADO -> documentosEnviados
            StatusDocumento.PARCIALMENTE_ASSINADO -> documentosParcialmenteAssinados
            StatusDocumento.CONCLUIDO -> documentosConcluidos
            StatusDocumento.RASCUNHO -> emptyList()
        }
}

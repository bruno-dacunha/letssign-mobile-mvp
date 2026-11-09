package br.com.fiap.letssign.presentation.signatario.dashboard

import br.com.fiap.letssign.domain.model.ResumoDocumentoSignatario
import br.com.fiap.letssign.domain.model.StatusAssinatura
import br.com.fiap.letssign.domain.model.Usuario

data class DashboardSignatarioUiState(
    val usuarioSignatario: Usuario? = null,
    val todosDocumentos: List<ResumoDocumentoSignatario> = emptyList(),
    val abaSelecionada: StatusAssinatura = StatusAssinatura.PENDENTE,
    val carregando: Boolean = true
) {
    val documentosPendentes: List<ResumoDocumentoSignatario>
        get() = todosDocumentos.filter { it.signatarioDocumento.status == StatusAssinatura.PENDENTE }

    val documentosAssinados: List<ResumoDocumentoSignatario>
        get() = todosDocumentos.filter { it.signatarioDocumento.status == StatusAssinatura.ASSINADO }

    val documentosDaAbaAtual: List<ResumoDocumentoSignatario>
        get() = when (abaSelecionada) {
            StatusAssinatura.PENDENTE -> documentosPendentes
            StatusAssinatura.ASSINADO -> documentosAssinados
        }
}

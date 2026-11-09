package br.com.fiap.letssign.presentation.navegacao

sealed class RotasTela(val rota: String) {
    data object Login : RotasTela("login")
    data object DashboardAdmin : RotasTela("admin/dashboard/{adminId}") {
        fun criarRota(adminId: String) = "admin/dashboard/$adminId"
    }
    data object GestaoSignatarios : RotasTela("admin/signatarios/{adminId}") {
        fun criarRota(adminId: String) = "admin/signatarios/$adminId"
    }
    data object WizardCriacaoDocumento : RotasTela("admin/documento/novo/{adminId}") {
        fun criarRota(adminId: String) = "admin/documento/novo/$adminId"
    }
    data object AcompanhamentoDocumento : RotasTela("admin/documento/acompanhamento/{documentoId}") {
        fun criarRota(documentoId: String) = "admin/documento/acompanhamento/$documentoId"
    }
    data object DashboardSignatario : RotasTela("signatario/dashboard/{signatarioId}") {
        fun criarRota(signatarioId: String) = "signatario/dashboard/$signatarioId"
    }
    data object AssinaturaDocumento : RotasTela("signatario/assinar/{documentoId}/{signatarioId}") {
        fun criarRota(documentoId: String, signatarioId: String) =
            "signatario/assinar/$documentoId/$signatarioId"
    }
}

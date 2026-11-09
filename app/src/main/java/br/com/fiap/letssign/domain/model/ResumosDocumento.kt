package br.com.fiap.letssign.domain.model

data class ResumoDocumentoAdmin(
    val documento: Documento,
    val totalSignatarios: Int,
    val totalAssinados: Int,
    val nomesSignatarios: List<String>
)

data class ResumoDocumentoSignatario(
    val documento: Documento,
    val signatarioDocumento: SignatarioDocumento,
    val totalCamposParaAssinar: Int,
    val totalCamposAssinados: Int
)

data class DetalhesAcompanhamentoDocumento(
    val documento: Documento,
    val signatarios: List<SignatarioDocumento>,
    val camposAssinatura: List<CampoAssinatura>,
    val historicoEventos: List<EventoDocumento>
)

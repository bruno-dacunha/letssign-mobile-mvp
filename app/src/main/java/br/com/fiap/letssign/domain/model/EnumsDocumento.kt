package br.com.fiap.letssign.domain.model

enum class StatusDocumento(val tituloExibicao: String) {
    RASCUNHO("Rascunho"),
    ENVIADO("Enviados"),
    PARCIALMENTE_ASSINADO("Parcialmente Assinados"),
    CONCLUIDO("Concluídos")
}

enum class PapelSignatario(val descricaoExibicao: String) {
    CONTRATANTE("Contratante"),
    CONTRATADO("Contratado"),
    TESTEMUNHA("Testemunha")
}

enum class StatusAssinatura(val descricaoExibicao: String) {
    PENDENTE("Pendente"),
    ASSINADO("Assinado")
}

enum class TipoEventoDocumento(val descricaoExibicao: String) {
    CRIADO("Documento Criado"),
    ENVIADO("Solicitações Enviadas"),
    FALHA_ENVIO("Falha no Envio"),
    ASSINADO("Assinatura Realizada"),
    CONCLUIDO("Fluxo Concluído")
}

package br.com.fiap.letssign.domain.model

data class Documento(
    val id: String,
    val adminId: String,
    val titulo: String,
    val caminhoPdfOriginal: String,
    val caminhoPdfAtual: String,
    val totalPaginas: Int,
    val mensagemConvite: String,
    val status: StatusDocumento,
    val versao: Int,
    val criadoEm: Long,
    val enviadoEm: Long? = null,
    val concluidoEm: Long? = null
)

data class SignatarioDocumento(
    val id: String,
    val documentoId: String,
    val signatarioId: String,
    val nomeSignatario: String,
    val emailSignatario: String,
    val telefoneSignatario: String,
    val papel: PapelSignatario,
    val obrigatorio: Boolean = true,
    val status: StatusAssinatura = StatusAssinatura.PENDENTE,
    val caminhoImagemAssinatura: String? = null,
    val assinadoEm: Long? = null
)

data class CampoAssinatura(
    val id: String,
    val documentoId: String,
    val signatarioDocumentoId: String,
    val pagina: Int,
    val posicaoXRelativa: Float,
    val posicaoYRelativa: Float,
    val larguraRelativa: Float,
    val alturaRelativa: Float,
    val assinado: Boolean = false,
    val assinadoEm: Long? = null
)

data class EventoDocumento(
    val id: String,
    val documentoId: String,
    val usuarioAtorId: String,
    val tipoEvento: TipoEventoDocumento,
    val descricao: String,
    val dataHora: Long
)

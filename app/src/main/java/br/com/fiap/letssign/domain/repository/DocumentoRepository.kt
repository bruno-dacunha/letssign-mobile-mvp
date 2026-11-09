package br.com.fiap.letssign.domain.repository

import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.model.CampoAssinatura
import br.com.fiap.letssign.domain.model.DetalhesAcompanhamentoDocumento
import br.com.fiap.letssign.domain.model.Documento
import br.com.fiap.letssign.domain.model.PapelSignatario
import br.com.fiap.letssign.domain.model.ResumoDocumentoAdmin
import br.com.fiap.letssign.domain.model.ResumoDocumentoSignatario
import kotlinx.coroutines.flow.Flow

data class SelecaoSignatarioInput(
    val signatarioId: String,
    val papel: PapelSignatario,
    val obrigatorio: Boolean = true
)

data class PosicaoCampoInput(
    val id: String,
    val signatarioId: String,
    val pagina: Int,
    val posicaoXRelativa: Float,
    val posicaoYRelativa: Float,
    val larguraRelativa: Float,
    val alturaRelativa: Float
)

interface DocumentoRepository {

    fun observarResumoDocumentosDoAdmin(adminId: String): Flow<List<ResumoDocumentoAdmin>>

    fun observarResumoDocumentosDoSignatario(signatarioId: String): Flow<List<ResumoDocumentoSignatario>>

    fun observarDetalhesAcompanhamento(documentoId: String): Flow<DetalhesAcompanhamentoDocumento?>

    suspend fun criarRascunhoEEnviarDocumento(
        adminId: String,
        titulo: String,
        caminhoPdfImportado: String,
        totalPaginas: Int,
        mensagemConvite: String,
        signatariosSelecionados: List<SelecaoSignatarioInput>,
        camposPosicionados: List<PosicaoCampoInput>,
        documentoIdExistenteParaReenvio: String? = null
    ): ResultadoOperacao<Documento>

    suspend fun registrarAssinaturaDoSignatario(
        documentoId: String,
        signatarioId: String,
        caminhoPdfAssinado: String,
        caminhoImagemRubrica: String
    ): ResultadoOperacao<Documento>

    suspend fun listarCamposDoDocumento(documentoId: String): List<CampoAssinatura>
}

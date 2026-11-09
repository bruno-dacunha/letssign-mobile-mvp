package br.com.fiap.letssign.data.repository

import androidx.room.withTransaction
import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.data.local.dao.CampoAssinaturaDao
import br.com.fiap.letssign.data.local.dao.DocumentoDao
import br.com.fiap.letssign.data.local.dao.EventoDocumentoDao
import br.com.fiap.letssign.data.local.dao.UsuarioDao
import br.com.fiap.letssign.data.local.db.LetsSignDatabase
import br.com.fiap.letssign.data.local.entity.CampoAssinaturaEntity
import br.com.fiap.letssign.data.local.entity.DocumentoEntity
import br.com.fiap.letssign.data.local.entity.EventoDocumentoEntity
import br.com.fiap.letssign.data.local.entity.SignatarioDocumentoEntity
import br.com.fiap.letssign.data.mapper.paraDominio
import br.com.fiap.letssign.domain.model.CampoAssinatura
import br.com.fiap.letssign.domain.model.DetalhesAcompanhamentoDocumento
import br.com.fiap.letssign.domain.model.Documento
import br.com.fiap.letssign.domain.model.ResumoDocumentoAdmin
import br.com.fiap.letssign.domain.model.ResumoDocumentoSignatario
import br.com.fiap.letssign.domain.model.StatusAssinatura
import br.com.fiap.letssign.domain.model.StatusDocumento
import br.com.fiap.letssign.domain.model.TipoEventoDocumento
import br.com.fiap.letssign.domain.repository.DocumentoRepository
import br.com.fiap.letssign.domain.repository.PosicaoCampoInput
import br.com.fiap.letssign.domain.repository.SelecaoSignatarioInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentoRepositoryImpl @Inject constructor(
    private val database: LetsSignDatabase,
    private val documentoDao: DocumentoDao,
    private val usuarioDao: UsuarioDao,
    private val campoAssinaturaDao: CampoAssinaturaDao,
    private val eventoDocumentoDao: EventoDocumentoDao
) : DocumentoRepository {

    override fun observarResumoDocumentosDoAdmin(adminId: String): Flow<List<ResumoDocumentoAdmin>> {
        return documentoDao.observarDocumentosDoAdmin(adminId).map { documentos ->
            documentos.map { docEntity ->
                val signatariosDoc = documentoDao.listarSignatariosDoDocumento(docEntity.id)
                val nomes = signatariosDoc.mapNotNull { sd ->
                    usuarioDao.buscarPorId(sd.signatarioId)?.nomeCompleto
                }
                val assinados = signatariosDoc.count { it.status == StatusAssinatura.ASSINADO.name }

                ResumoDocumentoAdmin(
                    documento = docEntity.paraDominio(),
                    totalSignatarios = signatariosDoc.size,
                    totalAssinados = assinados,
                    nomesSignatarios = nomes
                )
            }
        }
    }

    override fun observarResumoDocumentosDoSignatario(signatarioId: String): Flow<List<ResumoDocumentoSignatario>> {
        return documentoDao.observarDocumentosDoSignatario(signatarioId).map { documentos ->
            val usuarioSignatario = usuarioDao.buscarPorId(signatarioId)
            documentos.mapNotNull { docEntity ->
                val participacao = documentoDao.listarSignatariosDoDocumento(docEntity.id)
                    .firstOrNull { it.signatarioId == signatarioId }
                    ?: return@mapNotNull null

                val camposParticipante = campoAssinaturaDao.listarCamposDoParticipante(participacao.id)

                ResumoDocumentoSignatario(
                    documento = docEntity.paraDominio(),
                    signatarioDocumento = participacao.paraDominio(usuarioSignatario),
                    totalCamposParaAssinar = camposParticipante.size,
                    totalCamposAssinados = camposParticipante.count { it.assinado }
                )
            }
        }
    }

    override fun observarDetalhesAcompanhamento(documentoId: String): Flow<DetalhesAcompanhamentoDocumento?> {
        return combine(
            documentoDao.observarDocumentoPorId(documentoId),
            documentoDao.observarSignatariosDoDocumento(documentoId),
            campoAssinaturaDao.observarCamposDoDocumento(documentoId),
            eventoDocumentoDao.observarEventosDoDocumento(documentoId)
        ) { docEntity, signatariosEntities, camposEntities, eventosEntities ->
            if (docEntity == null) return@combine null

            val signatariosDominio = signatariosEntities.map { sd ->
                val usuario = usuarioDao.buscarPorId(sd.signatarioId)
                sd.paraDominio(usuario)
            }

            DetalhesAcompanhamentoDocumento(
                documento = docEntity.paraDominio(),
                signatarios = signatariosDominio,
                camposAssinatura = camposEntities.map { it.paraDominio() },
                historicoEventos = eventosEntities.map { it.paraDominio() }
            )
        }
    }

    override suspend fun criarRascunhoEEnviarDocumento(
        adminId: String,
        titulo: String,
        caminhoPdfImportado: String,
        totalPaginas: Int,
        mensagemConvite: String,
        signatariosSelecionados: List<SelecaoSignatarioInput>,
        camposPosicionados: List<PosicaoCampoInput>,
        documentoIdExistenteParaReenvio: String?
    ): ResultadoOperacao<Documento> {
        return try {
            val agora = System.currentTimeMillis()
            val documentoId = documentoIdExistenteParaReenvio ?: UUID.randomUUID().toString()

            val documentoEntity = DocumentoEntity(
                id = documentoId,
                adminId = adminId,
                titulo = titulo.trim(),
                caminhoPdfOriginal = caminhoPdfImportado,
                caminhoPdfAtual = caminhoPdfImportado,
                totalPaginas = totalPaginas,
                mensagemConvite = mensagemConvite.trim(),
                status = StatusDocumento.ENVIADO.name,
                versao = 1,
                criadoEm = agora,
                enviadoEm = agora,
                concluidoEm = null
            )

            database.withTransaction {
                documentoDao.removerSignatariosDoDocumento(documentoId)
                campoAssinaturaDao.excluirCamposDoDocumento(documentoId)

                documentoDao.inserirDocumento(documentoEntity)

                val mapaSignatarioParaParticipacaoId = mutableMapOf<String, String>()
                val entidadesSignatarios = signatariosSelecionados.map { selecao ->
                    val participacaoId = UUID.randomUUID().toString()
                    mapaSignatarioParaParticipacaoId[selecao.signatarioId] = participacaoId
                    SignatarioDocumentoEntity(
                        id = participacaoId,
                        documentoId = documentoId,
                        signatarioId = selecao.signatarioId,
                        papel = selecao.papel.name,
                        obrigatorio = selecao.obrigatorio,
                        status = StatusAssinatura.PENDENTE.name,
                        caminhoImagemAssinatura = null,
                        assinadoEm = null
                    )
                }
                documentoDao.inserirSignatariosDocumento(entidadesSignatarios)

                val entidadesCampos = camposPosicionados.mapNotNull { campo ->
                    val signatarioDocId = mapaSignatarioParaParticipacaoId[campo.signatarioId]
                        ?: return@mapNotNull null
                    CampoAssinaturaEntity(
                        id = campo.id.ifBlank { UUID.randomUUID().toString() },
                        documentoId = documentoId,
                        signatarioDocumentoId = signatarioDocId,
                        pagina = campo.pagina,
                        posicaoXRelativa = campo.posicaoXRelativa.coerceIn(0f, 1f),
                        posicaoYRelativa = campo.posicaoYRelativa.coerceIn(0f, 1f),
                        larguraRelativa = campo.larguraRelativa.coerceIn(0.05f, 1f),
                        alturaRelativa = campo.alturaRelativa.coerceIn(0.03f, 1f),
                        assinado = false,
                        assinadoEm = null
                    )
                }
                campoAssinaturaDao.inserirCampos(entidadesCampos)

                eventoDocumentoDao.inserirEvento(
                    EventoDocumentoEntity(
                        id = UUID.randomUUID().toString(),
                        documentoId = documentoId,
                        usuarioAtorId = adminId,
                        tipoEvento = TipoEventoDocumento.CRIADO.name,
                        descricao = "Documento '${titulo.trim()}' preparado com ${entidadesSignatarios.size} signatário(s) e ${entidadesCampos.size} campo(s) demarcado(s).",
                        dataHora = agora - 500
                    )
                )

                eventoDocumentoDao.inserirEvento(
                    EventoDocumentoEntity(
                        id = UUID.randomUUID().toString(),
                        documentoId = documentoId,
                        usuarioAtorId = adminId,
                        tipoEvento = TipoEventoDocumento.ENVIADO.name,
                        descricao = "Solicitações de assinatura disparadas com sucesso aos participantes.",
                        dataHora = agora
                    )
                )
            }

            ResultadoOperacao.Sucesso(documentoEntity.paraDominio())
        } catch (e: Exception) {
            ResultadoOperacao.Erro("Falha ao enviar documento: ${e.localizedMessage}", e)
        }
    }

    override suspend fun registrarAssinaturaDoSignatario(
        documentoId: String,
        signatarioId: String,
        caminhoPdfAssinado: String,
        caminhoImagemRubrica: String
    ): ResultadoOperacao<Documento> {
        return try {
            val agora = System.currentTimeMillis()
            var documentoAtualizado: DocumentoEntity? = null

            database.withTransaction {
                val docAtual = documentoDao.buscarDocumentoPorId(documentoId)
                    ?: throw IllegalStateException("Documento não encontrado.")

                val todosParticipantes = documentoDao.listarSignatariosDoDocumento(documentoId)
                val participacaoAtual = todosParticipantes.firstOrNull { it.signatarioId == signatarioId }
                    ?: throw IllegalStateException("Signatário não vinculado a este documento.")

                val participacaoAssinada = participacaoAtual.copy(
                    status = StatusAssinatura.ASSINADO.name,
                    caminhoImagemAssinatura = caminhoImagemRubrica,
                    assinadoEm = agora
                )
                documentoDao.atualizarSignatarioDocumento(participacaoAssinada)

                val camposDoParticipante = campoAssinaturaDao.listarCamposDoParticipante(participacaoAtual.id)
                camposDoParticipante.forEach { campo ->
                    campoAssinaturaDao.atualizarCampo(
                        campo.copy(assinado = true, assinadoEm = agora)
                    )
                }

                val usuarioSignatario = usuarioDao.buscarPorId(signatarioId)
                val nomeExibicao = usuarioSignatario?.nomeCompleto ?: "Signatário"

                eventoDocumentoDao.inserirEvento(
                    EventoDocumentoEntity(
                        id = UUID.randomUUID().toString(),
                        documentoId = documentoId,
                        usuarioAtorId = signatarioId,
                        tipoEvento = TipoEventoDocumento.ASSINADO.name,
                        descricao = "$nomeExibicao (${participacaoAtual.papel}) assinou o documento.",
                        dataHora = agora
                    )
                )

                val participantesAtualizados = documentoDao.listarSignatariosDoDocumento(documentoId)
                val todosObrigatoriosAssinaram = participantesAtualizados
                    .filter { it.obrigatorio }
                    .all { it.status == StatusAssinatura.ASSINADO.name }

                val novoStatusDoc = if (todosObrigatoriosAssinaram) {
                    StatusDocumento.CONCLUIDO.name
                } else {
                    StatusDocumento.PARCIALMENTE_ASSINADO.name
                }

                val docFinal = docAtual.copy(
                    caminhoPdfAtual = caminhoPdfAssinado,
                    status = novoStatusDoc,
                    concluidoEm = if (todosObrigatoriosAssinaram) agora else null
                )
                documentoDao.atualizarDocumento(docFinal)
                documentoAtualizado = docFinal

                if (todosObrigatoriosAssinaram) {
                    eventoDocumentoDao.inserirEvento(
                        EventoDocumentoEntity(
                            id = UUID.randomUUID().toString(),
                            documentoId = documentoId,
                            usuarioAtorId = signatarioId,
                            tipoEvento = TipoEventoDocumento.CONCLUIDO.name,
                            descricao = "Todos os signatários obrigatórios assinaram. Documento finalizado.",
                            dataHora = agora + 200
                        )
                    )
                }
            }

            ResultadoOperacao.Sucesso(documentoAtualizado!!.paraDominio())
        } catch (e: Exception) {
            ResultadoOperacao.Erro("Erro ao registrar assinatura: ${e.localizedMessage}", e)
        }
    }

    override suspend fun listarCamposDoDocumento(documentoId: String): List<CampoAssinatura> {
        return campoAssinaturaDao.listarCamposDoDocumento(documentoId).map { it.paraDominio() }
    }
}

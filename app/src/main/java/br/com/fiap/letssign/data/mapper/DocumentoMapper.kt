package br.com.fiap.letssign.data.mapper

import br.com.fiap.letssign.data.local.entity.CampoAssinaturaEntity
import br.com.fiap.letssign.data.local.entity.DocumentoEntity
import br.com.fiap.letssign.data.local.entity.EventoDocumentoEntity
import br.com.fiap.letssign.data.local.entity.SignatarioDocumentoEntity
import br.com.fiap.letssign.data.local.entity.UsuarioEntity
import br.com.fiap.letssign.domain.model.CampoAssinatura
import br.com.fiap.letssign.domain.model.Documento
import br.com.fiap.letssign.domain.model.EventoDocumento
import br.com.fiap.letssign.domain.model.PapelSignatario
import br.com.fiap.letssign.domain.model.SignatarioDocumento
import br.com.fiap.letssign.domain.model.StatusAssinatura
import br.com.fiap.letssign.domain.model.StatusDocumento
import br.com.fiap.letssign.domain.model.TipoEventoDocumento

fun DocumentoEntity.paraDominio(): Documento = Documento(
    id = id,
    adminId = adminId,
    titulo = titulo,
    caminhoPdfOriginal = caminhoPdfOriginal,
    caminhoPdfAtual = caminhoPdfAtual,
    totalPaginas = totalPaginas,
    mensagemConvite = mensagemConvite,
    status = runCatching { StatusDocumento.valueOf(status) }.getOrDefault(StatusDocumento.ENVIADO),
    versao = versao,
    criadoEm = criadoEm,
    enviadoEm = enviadoEm,
    concluidoEm = concluidoEm
)

fun SignatarioDocumentoEntity.paraDominio(usuario: UsuarioEntity?): SignatarioDocumento =
    SignatarioDocumento(
        id = id,
        documentoId = documentoId,
        signatarioId = signatarioId,
        nomeSignatario = usuario?.nomeCompleto ?: "Signatário",
        emailSignatario = usuario?.email ?: "",
        telefoneSignatario = usuario?.telefone ?: "",
        papel = runCatching { PapelSignatario.valueOf(papel) }.getOrDefault(PapelSignatario.CONTRATANTE),
        obrigatorio = obrigatorio,
        status = runCatching { StatusAssinatura.valueOf(status) }.getOrDefault(StatusAssinatura.PENDENTE),
        caminhoImagemAssinatura = caminhoImagemAssinatura,
        assinadoEm = assinadoEm
    )

fun CampoAssinaturaEntity.paraDominio(): CampoAssinatura = CampoAssinatura(
    id = id,
    documentoId = documentoId,
    signatarioDocumentoId = signatarioDocumentoId,
    pagina = pagina,
    posicaoXRelativa = posicaoXRelativa,
    posicaoYRelativa = posicaoYRelativa,
    larguraRelativa = larguraRelativa,
    alturaRelativa = alturaRelativa,
    assinado = assinado,
    assinadoEm = assinadoEm
)

fun EventoDocumentoEntity.paraDominio(): EventoDocumento = EventoDocumento(
    id = id,
    documentoId = documentoId,
    usuarioAtorId = usuarioAtorId,
    tipoEvento = runCatching { TipoEventoDocumento.valueOf(tipoEvento) }.getOrDefault(TipoEventoDocumento.CRIADO),
    descricao = descricao,
    dataHora = dataHora
)

package br.com.fiap.letssign.data.mapper

import br.com.fiap.letssign.data.local.entity.UsuarioEntity
import br.com.fiap.letssign.domain.model.PerfilUsuario
import br.com.fiap.letssign.domain.model.Usuario

fun UsuarioEntity.paraDominio(): Usuario = Usuario(
    id = id,
    nomeCompleto = nomeCompleto,
    email = email,
    telefone = telefone,
    perfil = runCatching { PerfilUsuario.valueOf(perfil) }.getOrDefault(PerfilUsuario.SIGNATARIO),
    criadoPorAdminId = criadoPorAdminId,
    criadoEm = criadoEm
)

fun Usuario.paraEntity(senhaHash: String): UsuarioEntity = UsuarioEntity(
    id = id,
    nomeCompleto = nomeCompleto,
    email = email,
    telefone = telefone,
    senhaHash = senhaHash,
    perfil = perfil.name,
    criadoPorAdminId = criadoPorAdminId,
    criadoEm = criadoEm
)

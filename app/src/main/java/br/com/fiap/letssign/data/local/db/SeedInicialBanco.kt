package br.com.fiap.letssign.data.local.db

import br.com.fiap.letssign.data.local.entity.UsuarioEntity
import br.com.fiap.letssign.domain.model.PerfilUsuario

object SeedInicialBanco {

    const val ID_ADMIN_PADRAO = "00000000-0000-0000-0000-000000000001"
    const val ID_SIGNATARIO_JOAO = "00000000-0000-0000-0000-000000000002"
    const val ID_SIGNATARIO_MARIA = "00000000-0000-0000-0000-000000000003"
    const val ID_SIGNATARIO_CARLOS = "00000000-0000-0000-0000-000000000004"

    fun obterUsuariosIniciais(agora: Long = System.currentTimeMillis()): List<UsuarioEntity> = listOf(
        UsuarioEntity(
            id = ID_ADMIN_PADRAO,
            nomeCompleto = "Administrador LetsSign",
            email = "admin@letssign.com.br",
            telefone = "(11) 99999-0001",
            senhaHash = "123456",
            perfil = PerfilUsuario.ADMIN.name,
            criadoPorAdminId = null,
            criadoEm = agora
        ),
        UsuarioEntity(
            id = ID_SIGNATARIO_JOAO,
            nomeCompleto = "João Silva",
            email = "joao@cliente.com.br",
            telefone = "(11) 98888-1001",
            senhaHash = "123456",
            perfil = PerfilUsuario.SIGNATARIO.name,
            criadoPorAdminId = ID_ADMIN_PADRAO,
            criadoEm = agora
        ),
        UsuarioEntity(
            id = ID_SIGNATARIO_MARIA,
            nomeCompleto = "Maria Oliveira",
            email = "maria@empresa.com.br",
            telefone = "(11) 97777-2002",
            senhaHash = "123456",
            perfil = PerfilUsuario.SIGNATARIO.name,
            criadoPorAdminId = ID_ADMIN_PADRAO,
            criadoEm = agora
        ),
        UsuarioEntity(
            id = ID_SIGNATARIO_CARLOS,
            nomeCompleto = "Carlos Mendes",
            email = "carlos@parceiro.com.br",
            telefone = "(11) 96666-3003",
            senhaHash = "123456",
            perfil = PerfilUsuario.SIGNATARIO.name,
            criadoPorAdminId = ID_ADMIN_PADRAO,
            criadoEm = agora
        )
    )
}

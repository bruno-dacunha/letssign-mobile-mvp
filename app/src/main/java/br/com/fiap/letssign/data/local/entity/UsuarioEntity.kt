package br.com.fiap.letssign.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["criado_por_admin_id"])
    ]
)
data class UsuarioEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "nome_completo")
    val nomeCompleto: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "telefone")
    val telefone: String,

    @ColumnInfo(name = "senha_hash")
    val senhaHash: String,

    @ColumnInfo(name = "perfil")
    val perfil: String,

    @ColumnInfo(name = "criado_por_admin_id")
    val criadoPorAdminId: String? = null,

    @ColumnInfo(name = "criado_em")
    val criadoEm: Long
)

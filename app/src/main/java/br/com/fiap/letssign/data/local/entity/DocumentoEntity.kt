package br.com.fiap.letssign.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documentos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["admin_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["admin_id"]),
        Index(value = ["status"])
    ]
)
data class DocumentoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "admin_id")
    val adminId: String,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "caminho_pdf_original")
    val caminhoPdfOriginal: String,

    @ColumnInfo(name = "caminho_pdf_atual")
    val caminhoPdfAtual: String,

    @ColumnInfo(name = "total_paginas")
    val totalPaginas: Int,

    @ColumnInfo(name = "mensagem_convite")
    val mensagemConvite: String,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "versao")
    val versao: Int = 1,

    @ColumnInfo(name = "criado_em")
    val criadoEm: Long,

    @ColumnInfo(name = "enviado_em")
    val enviadoEm: Long? = null,

    @ColumnInfo(name = "concluido_em")
    val concluidoEm: Long? = null
)

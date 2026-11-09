package br.com.fiap.letssign.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "signatarios_documento",
    foreignKeys = [
        ForeignKey(
            entity = DocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["documento_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["signatario_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["documento_id"]),
        Index(value = ["signatario_id"])
    ]
)
data class SignatarioDocumentoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "documento_id")
    val documentoId: String,

    @ColumnInfo(name = "signatario_id")
    val signatarioId: String,

    @ColumnInfo(name = "papel")
    val papel: String,

    @ColumnInfo(name = "obrigatorio")
    val obrigatorio: Boolean = true,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "caminho_imagem_assinatura")
    val caminhoImagemAssinatura: String? = null,

    @ColumnInfo(name = "assinado_em")
    val assinadoEm: Long? = null
)

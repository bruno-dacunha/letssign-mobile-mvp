package br.com.fiap.letssign.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "campos_assinatura",
    foreignKeys = [
        ForeignKey(
            entity = DocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["documento_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SignatarioDocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["signatario_documento_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["documento_id"]),
        Index(value = ["signatario_documento_id"])
    ]
)
data class CampoAssinaturaEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "documento_id")
    val documentoId: String,

    @ColumnInfo(name = "signatario_documento_id")
    val signatarioDocumentoId: String,

    @ColumnInfo(name = "pagina")
    val pagina: Int,

    @ColumnInfo(name = "posicao_x_relativa")
    val posicaoXRelativa: Float,

    @ColumnInfo(name = "posicao_y_relativa")
    val posicaoYRelativa: Float,

    @ColumnInfo(name = "largura_relativa")
    val larguraRelativa: Float,

    @ColumnInfo(name = "altura_relativa")
    val alturaRelativa: Float,

    @ColumnInfo(name = "assinado")
    val assinado: Boolean = false,

    @ColumnInfo(name = "assinado_em")
    val assinadoEm: Long? = null
)

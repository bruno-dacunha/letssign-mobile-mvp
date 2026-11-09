package br.com.fiap.letssign.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "eventos_documento",
    foreignKeys = [
        ForeignKey(
            entity = DocumentoEntity::class,
            parentColumns = ["id"],
            childColumns = ["documento_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["documento_id"]),
        Index(value = ["usuario_ator_id"])
    ]
)
data class EventoDocumentoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "documento_id")
    val documentoId: String,

    @ColumnInfo(name = "usuario_ator_id")
    val usuarioAtorId: String,

    @ColumnInfo(name = "tipo_evento")
    val tipoEvento: String,

    @ColumnInfo(name = "descricao")
    val descricao: String,

    @ColumnInfo(name = "data_hora")
    val dataHora: Long
)

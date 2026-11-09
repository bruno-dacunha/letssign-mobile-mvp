package br.com.fiap.letssign.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.fiap.letssign.data.local.entity.EventoDocumentoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDocumentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirEvento(evento: EventoDocumentoEntity)

    @Query("SELECT * FROM eventos_documento WHERE documento_id = :documentoId ORDER BY data_hora DESC")
    fun observarEventosDoDocumento(documentoId: String): Flow<List<EventoDocumentoEntity>>
}

package br.com.fiap.letssign.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.fiap.letssign.data.local.entity.CampoAssinaturaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CampoAssinaturaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirCampos(campos: List<CampoAssinaturaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirCampo(campo: CampoAssinaturaEntity)

    @Update
    suspend fun atualizarCampo(campo: CampoAssinaturaEntity)

    @Query("DELETE FROM campos_assinatura WHERE id = :campoId")
    suspend fun excluirCampoPorId(campoId: String)

    @Query("DELETE FROM campos_assinatura WHERE documento_id = :documentoId")
    suspend fun excluirCamposDoDocumento(documentoId: String)

    @Query("SELECT * FROM campos_assinatura WHERE documento_id = :documentoId ORDER BY pagina ASC")
    fun observarCamposDoDocumento(documentoId: String): Flow<List<CampoAssinaturaEntity>>

    @Query("SELECT * FROM campos_assinatura WHERE documento_id = :documentoId ORDER BY pagina ASC")
    suspend fun listarCamposDoDocumento(documentoId: String): List<CampoAssinaturaEntity>

    @Query("SELECT * FROM campos_assinatura WHERE signatario_documento_id = :signatarioDocumentoId ORDER BY pagina ASC")
    suspend fun listarCamposDoParticipante(signatarioDocumentoId: String): List<CampoAssinaturaEntity>
}

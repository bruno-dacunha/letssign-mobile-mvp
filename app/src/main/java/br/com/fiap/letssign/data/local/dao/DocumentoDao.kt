package br.com.fiap.letssign.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.fiap.letssign.data.local.entity.DocumentoEntity
import br.com.fiap.letssign.data.local.entity.SignatarioDocumentoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirDocumento(documento: DocumentoEntity)

    @Update
    suspend fun atualizarDocumento(documento: DocumentoEntity)

    @Query("SELECT * FROM documentos WHERE id = :documentoId LIMIT 1")
    suspend fun buscarDocumentoPorId(documentoId: String): DocumentoEntity?

    @Query("SELECT * FROM documentos WHERE id = :documentoId LIMIT 1")
    fun observarDocumentoPorId(documentoId: String): Flow<DocumentoEntity?>

    @Query("SELECT * FROM documentos WHERE admin_id = :adminId AND status != 'RASCUNHO' ORDER BY criado_em DESC")
    fun observarDocumentosDoAdmin(adminId: String): Flow<List<DocumentoEntity>>

    @Query(
        """
        SELECT d.* FROM documentos d
        INNER JOIN signatarios_documento sd ON sd.documento_id = d.id
        WHERE sd.signatario_id = :signatarioId
          AND d.status IN ('ENVIADO', 'PARCIALMENTE_ASSINADO', 'CONCLUIDO')
        ORDER BY d.enviado_em DESC
        """
    )
    fun observarDocumentosDoSignatario(signatarioId: String): Flow<List<DocumentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirSignatariosDocumento(signatarios: List<SignatarioDocumentoEntity>)

    @Query("DELETE FROM signatarios_documento WHERE documento_id = :documentoId")
    suspend fun removerSignatariosDoDocumento(documentoId: String)

    @Query("SELECT * FROM signatarios_documento WHERE documento_id = :documentoId")
    fun observarSignatariosDoDocumento(documentoId: String): Flow<List<SignatarioDocumentoEntity>>

    @Query("SELECT * FROM signatarios_documento WHERE documento_id = :documentoId")
    suspend fun listarSignatariosDoDocumento(documentoId: String): List<SignatarioDocumentoEntity>

    @Update
    suspend fun atualizarSignatarioDocumento(signatarioDocumento: SignatarioDocumentoEntity)
}

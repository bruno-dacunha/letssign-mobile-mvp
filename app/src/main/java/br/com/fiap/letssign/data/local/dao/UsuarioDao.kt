package br.com.fiap.letssign.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.fiap.letssign.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun buscarPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun observarPorId(id: String): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios WHERE perfil = 'SIGNATARIO' AND (criado_por_admin_id = :adminId OR criado_por_admin_id IS NULL) ORDER BY nome_completo ASC")
    fun observarSignatariosDoAdmin(adminId: String): Flow<List<UsuarioEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserir(usuario: UsuarioEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserirIgnorandoConflito(usuarios: List<UsuarioEntity>)

    @Update
    suspend fun atualizar(usuario: UsuarioEntity)

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int
}

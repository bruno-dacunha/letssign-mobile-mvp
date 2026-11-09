package br.com.fiap.letssign.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.fiap.letssign.data.local.dao.CampoAssinaturaDao
import br.com.fiap.letssign.data.local.dao.DocumentoDao
import br.com.fiap.letssign.data.local.dao.EventoDocumentoDao
import br.com.fiap.letssign.data.local.dao.UsuarioDao
import br.com.fiap.letssign.data.local.entity.CampoAssinaturaEntity
import br.com.fiap.letssign.data.local.entity.DocumentoEntity
import br.com.fiap.letssign.data.local.entity.EventoDocumentoEntity
import br.com.fiap.letssign.data.local.entity.SignatarioDocumentoEntity
import br.com.fiap.letssign.data.local.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        DocumentoEntity::class,
        SignatarioDocumentoEntity::class,
        CampoAssinaturaEntity::class,
        EventoDocumentoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LetsSignDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun campoAssinaturaDao(): CampoAssinaturaDao
    abstract fun eventoDocumentoDao(): EventoDocumentoDao

    companion object {
        const val NOME_BANCO = "letssign_sprint1.db"
    }
}

package br.com.fiap.letssign.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.fiap.letssign.data.local.dao.CampoAssinaturaDao
import br.com.fiap.letssign.data.local.dao.DocumentoDao
import br.com.fiap.letssign.data.local.dao.EventoDocumentoDao
import br.com.fiap.letssign.data.local.dao.UsuarioDao
import br.com.fiap.letssign.data.local.db.LetsSignDatabase
import br.com.fiap.letssign.data.local.db.SeedInicialBanco
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BancoDadosModule {

    @Provides
    @Singleton
    fun proverBancoDados(
        @ApplicationContext context: Context,
        usuarioDaoProvider: Provider<UsuarioDao>
    ): LetsSignDatabase {
        return Room.databaseBuilder(
            context,
            LetsSignDatabase::class.java,
            LetsSignDatabase.NOME_BANCO
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        usuarioDaoProvider.get()
                            .inserirIgnorandoConflito(SeedInicialBanco.obterUsuariosIniciais())
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun proverUsuarioDao(database: LetsSignDatabase): UsuarioDao = database.usuarioDao()

    @Provides
    @Singleton
    fun proverDocumentoDao(database: LetsSignDatabase): DocumentoDao = database.documentoDao()

    @Provides
    @Singleton
    fun proverCampoAssinaturaDao(database: LetsSignDatabase): CampoAssinaturaDao =
        database.campoAssinaturaDao()

    @Provides
    @Singleton
    fun proverEventoDocumentoDao(database: LetsSignDatabase): EventoDocumentoDao =
        database.eventoDocumentoDao()
}

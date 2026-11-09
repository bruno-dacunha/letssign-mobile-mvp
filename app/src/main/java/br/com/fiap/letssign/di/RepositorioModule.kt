package br.com.fiap.letssign.di

import br.com.fiap.letssign.data.repository.ArquivoPdfRepositoryImpl
import br.com.fiap.letssign.data.repository.AutenticacaoRepositoryImpl
import br.com.fiap.letssign.data.repository.DocumentoRepositoryImpl
import br.com.fiap.letssign.data.repository.SignatarioRepositoryImpl
import br.com.fiap.letssign.domain.repository.ArquivoPdfRepository
import br.com.fiap.letssign.domain.repository.AutenticacaoRepository
import br.com.fiap.letssign.domain.repository.DocumentoRepository
import br.com.fiap.letssign.domain.repository.SignatarioRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositorioModule {

    @Binds
    @Singleton
    abstract fun vincularAutenticacaoRepository(
        implementacao: AutenticacaoRepositoryImpl
    ): AutenticacaoRepository

    @Binds
    @Singleton
    abstract fun vincularSignatarioRepository(
        implementacao: SignatarioRepositoryImpl
    ): SignatarioRepository

    @Binds
    @Singleton
    abstract fun vincularDocumentoRepository(
        implementacao: DocumentoRepositoryImpl
    ): DocumentoRepository

    @Binds
    @Singleton
    abstract fun vincularArquivoPdfRepository(
        implementacao: ArquivoPdfRepositoryImpl
    ): ArquivoPdfRepository
}

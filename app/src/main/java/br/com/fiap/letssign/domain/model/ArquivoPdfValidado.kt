package br.com.fiap.letssign.domain.model

data class ArquivoPdfValidado(
    val caminhoLocalPrivado: String,
    val nomeArquivoOriginal: String,
    val tamanhoBytes: Long,
    val totalPaginas: Int
)

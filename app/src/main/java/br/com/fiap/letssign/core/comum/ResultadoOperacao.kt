package br.com.fiap.letssign.core.comum

sealed class ResultadoOperacao<out T> {
    data class Sucesso<out T>(val dados: T) : ResultadoOperacao<T>()
    data class Erro(val mensagem: String, val causa: Throwable? = null) : ResultadoOperacao<Nothing>()
}

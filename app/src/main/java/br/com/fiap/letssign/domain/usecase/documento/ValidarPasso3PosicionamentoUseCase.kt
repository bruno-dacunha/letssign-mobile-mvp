package br.com.fiap.letssign.domain.usecase.documento

import br.com.fiap.letssign.core.comum.ResultadoOperacao
import br.com.fiap.letssign.domain.repository.PosicaoCampoInput
import br.com.fiap.letssign.domain.repository.SelecaoSignatarioInput
import javax.inject.Inject

class ValidarPasso3PosicionamentoUseCase @Inject constructor() {

    operator fun invoke(
        totalPaginasPdf: Int,
        signatariosSelecionados: List<SelecaoSignatarioInput>,
        camposPosicionados: List<PosicaoCampoInput>,
        nomesPorSignatarioId: Map<String, String>
    ): ResultadoOperacao<Unit> {
        if (camposPosicionados.isEmpty()) {
            return ResultadoOperacao.Erro(
                "Adicione e posicione ao menos 1 campo de assinatura sobre a página do PDF."
            )
        }

        val idsSignatariosSelecionados = signatariosSelecionados.map { it.signatarioId }.toSet()

        val signatariosObrigatoriosSemCampo = signatariosSelecionados
            .filter { it.obrigatorio }
            .filter { obrigatorio ->
                camposPosicionados.none { campo -> campo.signatarioId == obrigatorio.signatarioId }
            }

        if (signatariosObrigatoriosSemCampo.isNotEmpty()) {
            val nomesFaltantes = signatariosObrigatoriosSemCampo.joinToString(", ") {
                nomesPorSignatarioId[it.signatarioId] ?: "Signatário"
            }
            return ResultadoOperacao.Erro(
                "Posicione ao menos um campo de assinatura no PDF para: $nomesFaltantes."
            )
        }

        for (campo in camposPosicionados) {
            if (campo.signatarioId !in idsSignatariosSelecionados) {
                return ResultadoOperacao.Erro(
                    "Existe um campo de assinatura vinculado a um signatário não selecionado."
                )
            }
            if (campo.pagina !in 0 until totalPaginasPdf) {
                return ResultadoOperacao.Erro(
                    "Campo posicionado em página inválida (${campo.pagina + 1})."
                )
            }
            val dentroDosLimitesX = campo.posicaoXRelativa >= -0.001f &&
                (campo.posicaoXRelativa + campo.larguraRelativa) <= 1.001f
            val dentroDosLimitesY = campo.posicaoYRelativa >= -0.001f &&
                (campo.posicaoYRelativa + campo.alturaRelativa) <= 1.001f

            if (!dentroDosLimitesX || !dentroDosLimitesY) {
                return ResultadoOperacao.Erro(
                    "Todos os campos de assinatura devem permanecer dentro dos limites da página do PDF."
                )
            }
        }

        return ResultadoOperacao.Sucesso(Unit)
    }
}

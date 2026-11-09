package br.com.fiap.letssign.domain.usecase.documento

import br.com.fiap.letssign.domain.model.ResumoDocumentoAdmin
import br.com.fiap.letssign.domain.model.ResumoDocumentoSignatario
import br.com.fiap.letssign.domain.repository.DocumentoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObterDashboardAdminUseCase @Inject constructor(
    private val documentoRepository: DocumentoRepository
) {
    operator fun invoke(adminId: String): Flow<List<ResumoDocumentoAdmin>> =
        documentoRepository.observarResumoDocumentosDoAdmin(adminId)
}

class ObterDashboardSignatarioUseCase @Inject constructor(
    private val documentoRepository: DocumentoRepository
) {
    operator fun invoke(signatarioId: String): Flow<List<ResumoDocumentoSignatario>> =
        documentoRepository.observarResumoDocumentosDoSignatario(signatarioId)
}

package br.com.fiap.letssign.core.comum

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatadorData {
    private val formatoDataHora = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
    private val formatoDataCurta = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    fun formatarDataHora(timestamp: Long?): String {
        if (timestamp == null || timestamp <= 0L) return "-"
        return formatoDataHora.format(Date(timestamp))
    }

    fun formatarDataCurta(timestamp: Long?): String {
        if (timestamp == null || timestamp <= 0L) return "-"
        return formatoDataCurta.format(Date(timestamp))
    }
}

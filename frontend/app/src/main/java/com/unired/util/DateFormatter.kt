package com.unired.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DateFormatter {
    fun formatDateString(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "Desconocido"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateStr)
            
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            if (date != null) outputFormat.format(date) else "Desconocido"
        } catch (e: Exception) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateStr)
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                if (date != null) outputFormat.format(date) else "Desconocido"
            } catch (ex: Exception) {
                if (dateStr.length >= 10 && dateStr[4] == '-' && dateStr[7] == '-') {
                    val parts = dateStr.substring(0, 10).split("-")
                    if (parts.size == 3) {
                        "${parts[2]}/${parts[1]}/${parts[0]}"
                    } else {
                        dateStr
                    }
                } else {
                    dateStr
                }
            }
        }
    }
}

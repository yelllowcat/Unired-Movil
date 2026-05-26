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

    fun formatRelativeTime(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "Ahora"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = try {
                inputFormat.parse(dateStr)
            } catch (e: Exception) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(dateStr)
            }

            if (date != null) {
                val diffMs = System.currentTimeMillis() - date.time
                val diffSecs = diffMs / 1000
                val diffMins = diffSecs / 60
                val diffHours = diffMins / 60
                val diffDays = diffHours / 24

                when {
                    diffMins < 1 -> "Ahora"
                    diffMins < 60 -> "${diffMins} m"
                    diffHours < 24 -> "${diffHours} h"
                    diffDays < 7 -> "${diffDays} d"
                    else -> {
                        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        outputFormat.format(date)
                    }
                }
            } else {
                "Desconocido"
            }
        } catch (e: Exception) {
            formatDateString(dateStr)
        }
    }
}

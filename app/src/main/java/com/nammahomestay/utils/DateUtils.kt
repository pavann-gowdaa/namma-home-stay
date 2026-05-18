package com.nammahomestay.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private val dbFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.ENGLISH)
    private val displayFormat = SimpleDateFormat(Constants.DISPLAY_DATE_FORMAT, Locale.ENGLISH)

    fun getTodayDate(): String {
        return dbFormat.format(Date())
    }

    fun formatForDisplay(dateStr: String): String {
        return try {
            val date = dbFormat.parse(dateStr)
            displayFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        return try {
            displayFormat.format(Date(timestamp))
        } catch (e: Exception) {
            timestamp.toString()
        }
    }

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
}

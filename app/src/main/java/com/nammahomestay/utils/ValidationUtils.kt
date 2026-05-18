package com.nammahomestay.utils

object ValidationUtils {

    fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.trim().replace(" ", "").replace("-", "")
        return cleaned.length in 10..15 && cleaned.all { it.isDigit() || it == '+' }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPrice(price: String): Boolean {
        return price.toDoubleOrNull() != null && price.toDouble() > 0
    }

    fun isNotEmpty(vararg fields: String): Boolean {
        return fields.all { it.trim().isNotBlank() }
    }

    fun sanitizeInput(input: String): String {
        return input.trim().replace(Regex("<[^>]*>"), "")
    }
}

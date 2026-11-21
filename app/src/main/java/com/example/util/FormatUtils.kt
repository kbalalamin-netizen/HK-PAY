package com.example.util

import android.util.Patterns
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    fun formatMoney(amount: Double, currency: String = "USD"): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return when (currency.uppercase(Locale.US)) {
            "USD" -> "$${formatter.format(amount)}"
            "BDT" -> "৳${formatter.format(amount)}"
            "EUR" -> "€${formatter.format(amount)}"
            "GBP" -> "£${formatter.format(amount)}"
            else -> "${formatter.format(amount)} $currency"
        }
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0L) return "Pending"
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return "—"
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }
}

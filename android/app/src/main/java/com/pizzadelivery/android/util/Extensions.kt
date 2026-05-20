package com.pizzadelivery.android.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun Double.toCurrency(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(this)
}

fun String.toFormattedDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(this)
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        this
    }
}

fun String.toRelativeTime(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(this) ?: return this
        val diff = System.currentTimeMillis() - date.time
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> {
                val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        this
    }
}

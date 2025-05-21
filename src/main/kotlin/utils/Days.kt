package com.example.utils

object Days {

    fun formatDaysLeft(daysLeft: Long): String {
        val dayWord = when {
            daysLeft % 100 in 11..14 -> "дней"
            daysLeft % 10 == 1L -> "день"
            daysLeft % 10 in 2..4 -> "дня"
            else -> "дней"
        }
        return "$daysLeft $dayWord"
    }
}

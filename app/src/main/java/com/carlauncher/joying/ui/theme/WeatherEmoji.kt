package com.carlauncher.joying.ui

fun weatherEmoji(icon: String): String {
    return when (icon.take(2)) {
        "01" -> "☀"
        "02" -> "🌤"
        "03", "04" -> "☁"
        "09", "10" -> "🌧"
        "11" -> "⛈"
        "13" -> "❄"
        "50" -> "🌫"
        else -> "🌡"
    }
}
package com.pokesocial.app.ui.util

import java.util.concurrent.TimeUnit

fun formatCount(n: Int): String = when {
    n >= 1_000_000 -> String.format("%.1fM", n / 1_000_000.0)
    n >= 1_000 -> String.format("%.1fK", n / 1_000.0)
    else -> n.toString()
}

fun timeAgo(createdAt: Long): String {
    val diff = System.currentTimeMillis() - createdAt
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        minutes < 1 -> "agora"
        minutes < 60 -> "${minutes} min"
        hours < 24 -> "${hours} h"
        days < 7 -> "${days} d"
        else -> "${days / 7} sem"
    }
}

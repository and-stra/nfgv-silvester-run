package com.nfgv.stopwatch.util

fun Long.toHHMMSSs(): String {
    val tenths = (this / 100).toInt() % 10

    return String.format("${toHHMMSS()}.%d", tenths)
}

fun Long.toHHMMSS(): String {
    val seconds = (this / 1000).toInt() % 60
    val minutes = (this / (1000 * 60)).toInt() % 60
    val hours = (this / (1000 * 3600)).toInt() % 24

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun Long.toCET(): Long {
    return 3600 * 1000 * 2 + this
}
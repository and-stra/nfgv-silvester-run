package com.nfgv.stopwatch.util.extensions

import android.content.Context

fun Context.getApplicationName(): String {
    return packageManager.getApplicationLabel(applicationInfo).toString()
}
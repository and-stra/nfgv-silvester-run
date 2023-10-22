package com.nfgv.stopwatch.util

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun runOnUIThread(runnable: Runnable) {
    Handler(Looper.getMainLooper()).post(runnable)
}

fun runOnCoroutineThread(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch(context, start, block)
}
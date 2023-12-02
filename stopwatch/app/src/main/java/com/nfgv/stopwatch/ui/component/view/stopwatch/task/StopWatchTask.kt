package com.nfgv.stopwatch.ui.component.view.stopwatch.task

import com.nfgv.stopwatch.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StopWatchTask(
    private val runStartTimestamp: Long
) {
    private var job: Job? = null

    private val _resultFlow = MutableSharedFlow<Unit>()

    val resultFlow = _resultFlow.asSharedFlow()

    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                _resultFlow.emit(Unit)

                delay(Constants.STOPWATCH_TASK_INTERVAL)
            }
        }
    }

    fun stop() {
        job?.cancel()
    }
}
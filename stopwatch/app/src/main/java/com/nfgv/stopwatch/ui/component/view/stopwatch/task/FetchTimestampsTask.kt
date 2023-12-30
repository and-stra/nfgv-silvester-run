package com.nfgv.stopwatch.ui.component.view.stopwatch.task

import com.nfgv.stopwatch.data.domain.response.GoogleSheetsReadDataApiResponse
import com.nfgv.stopwatch.data.service.FetchTimestampsService
import com.nfgv.stopwatch.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FetchTimestampsTask(
    private val fetchTimestampsService: FetchTimestampsService,
    private val sheetsId: String,
    private val stopperId: String
) {
    private var job: Job? = null

    private val _resultFlow = MutableSharedFlow<GoogleSheetsReadDataApiResponse>()

    val resultFlow = _resultFlow.asSharedFlow()

    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                _resultFlow.emit(fetchTimestampsService.fetch(sheetsId, stopperId))

                delay(Constants.FETCH_TIMESTAMPS_TASK_INTERVAL)
            }
        }
    }

    fun stop() {
        job?.cancel()
    }
}
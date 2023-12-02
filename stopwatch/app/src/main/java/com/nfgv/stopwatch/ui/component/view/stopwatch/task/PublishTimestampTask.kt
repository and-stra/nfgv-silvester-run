package com.nfgv.stopwatch.ui.component.view.stopwatch.task

import com.nfgv.stopwatch.data.service.PublishTimestampsService
import com.nfgv.stopwatch.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class Timestamp(val value: Long)

class PublishTimestampTask(
    private val publishTimestampsService: PublishTimestampsService,
    private val sheetsId: String,
    private val stopperId: String
) {
    private val queuedTimestamps = ArrayList<Timestamp>()

    private var job: Job? = null

    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                batchPublishTimestamps()
                delay(Constants.PUBLISH_TIMESTAMPS_TASK_INTERVAL)
            }
        }
    }

    fun stop() {
        job?.cancel()
    }

    fun queue(value: Long) {
        synchronized(queuedTimestamps) {
            queuedTimestamps.add(Timestamp(value))
        }
    }

    private fun dequeueAll(): List<List<Long>> {
        var timestamps = emptyList<List<Long>>()

        synchronized(queuedTimestamps) {
            timestamps = queuedTimestamps.map { listOf(it.copy().value) }
            queuedTimestamps.clear()
        }

        return timestamps
    }

    private suspend fun batchPublishTimestamps() {
        dequeueAll().also { timestampsToPublish ->
            if (timestampsToPublish.isNotEmpty()) {
                publishTimestampsService.publish(sheetsId, "$stopperId!A:A", timestampsToPublish)
            }
        }
    }
}
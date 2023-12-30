package com.nfgv.stopwatch.ui.component.view.stopwatch.task

import com.nfgv.stopwatch.data.domain.response.GoogleSheetsAppendDataApiResponse
import com.nfgv.stopwatch.data.service.BackupTimestampsService
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

    fun cancel() {
        job?.cancel()
    }

    fun queue(value: Long) {
        synchronized(queuedTimestamps) {
            queuedTimestamps.add(Timestamp(value))
        }
    }

    private fun dequeueAll(): List<Long> {
        var timestamps = emptyList<Long>()

        synchronized(queuedTimestamps) {
            timestamps = queuedTimestamps.map { it.copy().value }
            queuedTimestamps.clear()
        }

        return timestamps
    }

    private fun requeue(timestampsToRequeue: List<Long>) {
        synchronized(queuedTimestamps) {
            queuedTimestamps.addAll(0, timestampsToRequeue.map { Timestamp(it) })
        }
    }

    private suspend fun batchPublishTimestamps() {
        dequeueAll().also { timestampsToPublish ->
            if (timestampsToPublish.isNotEmpty()) {
                val result = publishTimestampsService.publish(
                    sheetsId,
                    stopperId,
                    timestampsToPublish.map { listOf(it) })

                if (result is GoogleSheetsAppendDataApiResponse.Error) {
                    requeue(timestampsToPublish)
                }
            }
        }
    }
}
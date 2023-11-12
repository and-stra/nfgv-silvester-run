package com.nfgv.stopwatch.service

import com.nfgv.stopwatch.util.runOnCoroutineThread
import kotlinx.coroutines.coroutineScope

private data class Timestamp(val value: Long)

class PublishTimestampService private constructor() {
    private val queuedTimestamps = ArrayList<Timestamp>()
    private val googleSheetService = GoogleSheetService.instance

    companion object {
        val instance: PublishTimestampService by lazy {
            PublishTimestampService()
        }
    }

    fun queue(value: Long) {
        synchronized(queuedTimestamps) {
            queuedTimestamps.add(Timestamp(value))
        }
    }

    fun publish(sheetId: String, stopperId: String) {
        runOnCoroutineThread {
            coroutineScope {
                var timestampsToPublish = emptyList<List<Long>>()

                synchronized(queuedTimestamps) {
                    timestampsToPublish = queuedTimestamps.map { listOf(it.copy().value) }
                    queuedTimestamps.clear()
                }

                googleSheetService.appendValues(sheetId, "$stopperId!A:A", timestampsToPublish)
            }
        }
    }
}
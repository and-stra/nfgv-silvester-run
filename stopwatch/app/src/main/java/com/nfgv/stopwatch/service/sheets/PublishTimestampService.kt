package com.nfgv.stopwatch.service.sheets

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
        var timestampsToPublish = emptyList<List<Long>>()

        synchronized(queuedTimestamps) {
            timestampsToPublish = queuedTimestamps.map { listOf(it.copy().value) }
            queuedTimestamps.clear()
        }

        if (timestampsToPublish.isNotEmpty()) {
            googleSheetService.appendValues(sheetId, "$stopperId!A:A", timestampsToPublish)
        }
    }
}
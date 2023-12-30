package com.nfgv.stopwatch.data.service

import com.nfgv.stopwatch.data.repository.local.InternalStorageRepository
import com.nfgv.stopwatch.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupTimestampsService @Inject constructor(
    private val internalStorageRepository: InternalStorageRepository
) {
    fun store(timestamps: List<Long>) {
        return internalStorageRepository.writeToFile(
            Constants.BACKUP_FILE_NAME,
            timestamps.fold("") { accumulated, timestamp ->
                StringBuilder()
                    .append(accumulated)
                    .append("${timestamp}\n")
                    .toString()
            }
        )
    }

    fun read(): List<Long> {
        val content = internalStorageRepository.readFile(Constants.BACKUP_FILE_NAME)

        return content?.split("\n")?.filter { it.isNotEmpty() }?.map {
            try {
                it.toLong()
            } catch (e: NumberFormatException) {
                0L
            }
        } ?: emptyList()
    }

    fun deleteAll() {
        internalStorageRepository.deleteFile(Constants.BACKUP_FILE_NAME)
    }
}
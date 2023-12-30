package com.nfgv.stopwatch.data.service

import com.nfgv.stopwatch.data.domain.response.GoogleSheetsAppendDataApiResponse
import com.nfgv.stopwatch.data.repository.remote.GoogleSheetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadBackupDataService @Inject constructor(
    private val googleSheetsRepository: GoogleSheetsRepository
) {
    suspend fun upload(
        sheetsId: String,
        sheetTitle: String,
        timestamps: List<List<Long>>
    ): GoogleSheetsAppendDataApiResponse {
        return googleSheetsRepository.appendValues(sheetsId, "$sheetTitle!A:A", timestamps)
    }
}
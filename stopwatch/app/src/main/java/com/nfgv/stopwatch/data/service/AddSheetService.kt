package com.nfgv.stopwatch.data.service

import com.nfgv.stopwatch.data.domain.response.GoogleSheetsAddSheetApiResponse
import com.nfgv.stopwatch.data.repository.remote.GoogleSheetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddSheetService @Inject constructor(
    private val googleSheetsRepository: GoogleSheetsRepository
) {
    suspend fun add(sheetsId: String, title: String): GoogleSheetsAddSheetApiResponse {
        return googleSheetsRepository.addSheet(sheetsId, title)
    }
}
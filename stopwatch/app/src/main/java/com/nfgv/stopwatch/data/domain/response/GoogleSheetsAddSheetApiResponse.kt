package com.nfgv.stopwatch.data.domain.response

import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse

sealed class GoogleSheetsAddSheetApiResponse {
    data class Ok(
        val response: BatchUpdateSpreadsheetResponse,
        val sheetTitle: String
    ) : GoogleSheetsAddSheetApiResponse()

    data class Error(val errorMessage: String?) : GoogleSheetsAddSheetApiResponse()
}
package com.nfgv.stopwatch.data.domain.response

import com.google.api.services.sheets.v4.model.AppendValuesResponse

sealed class GoogleSheetsPostApiResponse {
    data class Ok(val response: AppendValuesResponse?) : GoogleSheetsPostApiResponse()
    data class Error(val errorMessage: String?) : GoogleSheetsPostApiResponse()
}
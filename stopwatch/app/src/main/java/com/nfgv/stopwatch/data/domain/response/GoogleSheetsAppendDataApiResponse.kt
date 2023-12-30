package com.nfgv.stopwatch.data.domain.response

import com.google.api.services.sheets.v4.model.AppendValuesResponse

sealed class GoogleSheetsAppendDataApiResponse {
    data class Ok(val response: AppendValuesResponse?) : GoogleSheetsAppendDataApiResponse()
    data class Error(val errorMessage: String?) : GoogleSheetsAppendDataApiResponse()
}
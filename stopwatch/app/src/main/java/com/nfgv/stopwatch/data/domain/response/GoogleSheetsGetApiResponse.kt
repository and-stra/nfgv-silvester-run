package com.nfgv.stopwatch.data.domain.response

sealed class GoogleSheetsGetApiResponse {
    data class Ok(val data: List<List<String>>) : GoogleSheetsGetApiResponse()
    data class Error(val errorMessage: String?) : GoogleSheetsGetApiResponse()
}
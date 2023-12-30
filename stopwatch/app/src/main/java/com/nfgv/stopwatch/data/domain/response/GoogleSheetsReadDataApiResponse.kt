package com.nfgv.stopwatch.data.domain.response

sealed class GoogleSheetsReadDataApiResponse {
    data class Ok(val data: List<List<String>>) : GoogleSheetsReadDataApiResponse()
    data class Error(val errorMessage: String?) : GoogleSheetsReadDataApiResponse()
}
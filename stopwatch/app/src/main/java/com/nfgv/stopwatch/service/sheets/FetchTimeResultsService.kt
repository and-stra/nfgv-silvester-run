package com.nfgv.stopwatch.service.sheets

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class FetchTimeResultsService private constructor() {
    private val googleSheetService = GoogleSheetService.instance

    companion object {
        val instance: FetchTimeResultsService by lazy {
            FetchTimeResultsService()
        }
    }

    suspend fun fetch(sheetId: String, stopperId: String): Array<String> {
        return coroutineScope {
            return@coroutineScope async {
                val result = async {
                    googleSheetService.readValues(sheetId, "$stopperId!A:A")
                }.await()

                return@async result?.subList(1, result.size)?.map { sublist ->
                    if (sublist.isEmpty()) "" else sublist[0].toString()
                }?.toTypedArray() ?: emptyArray()
            }.await()
        }
    }
}
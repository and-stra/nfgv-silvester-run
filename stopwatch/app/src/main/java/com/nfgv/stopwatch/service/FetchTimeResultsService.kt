package com.nfgv.stopwatch.service

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class FetchTimeResultsService private constructor() {
    private val googleSheetService = GoogleSheetService.instance

    companion object {
        val instance: FetchTimeResultsService by lazy {
            FetchTimeResultsService()
        }
    }

    suspend fun fetch(sheetId: String): Array<String> {
        return coroutineScope {
            return@coroutineScope async {
                val result = async {
                    googleSheetService.readValues(sheetId, "Laufzeiten!C:C")
                }.await()

                println(result?.subList(1, result.size))

                return@async result?.subList(1, result.size)?.mapIndexed { index, sublist ->
                    "${index + 1}    ${if (sublist.isEmpty()) "--:--:--.-" else sublist[0]}"
                }?.toTypedArray() ?: emptyArray()
            }.await()
        }
    }
}
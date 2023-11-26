package com.nfgv.stopwatch.service.sheets

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.StringBuilder

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

                return@async result?.subList(1, result.size)?.mapIndexed { index, sublist ->
                    StringBuilder()
                        .append(String.format("%3d", index + 1))
                        .append("                  ")
                        .append(if (sublist.isEmpty()) "" else sublist[0])
                        .toString()
                }?.toTypedArray() ?: emptyArray()
            }.await()
        }
    }
}
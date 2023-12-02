package com.nfgv.stopwatch.data.repository.remote

import com.google.api.services.sheets.v4.model.ValueRange
import com.nfgv.stopwatch.data.service.ProvideGoogleSheetsClientService
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsGetApiResponse
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsPostApiResponse
import com.nfgv.stopwatch.data.service.ValueInputOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSheetsRepository @Inject constructor(
    private val provideSheetsClientService: ProvideGoogleSheetsClientService
) {
    suspend fun readValues(sheetId: String, range: String): GoogleSheetsGetApiResponse {
        return coroutineScope {
            async {
                return@async try {
                    val result = provideSheetsClientService.getClient().spreadsheets()?.values()
                        ?.get(sheetId, range)
                        ?.execute()
                        ?.getValues()

                    GoogleSheetsGetApiResponse.Ok(result.toData())
                } catch (e: Exception) {
                    GoogleSheetsGetApiResponse.Error(e.message)
                }
            }.await()
        }
    }

    suspend fun appendValues(
        sheetId: String,
        range: String,
        values: List<List<Any>>
    ): GoogleSheetsPostApiResponse {
        val body = ValueRange().setValues(values).setRange(range)

        return coroutineScope {
            async {
                return@async try {
                    val result = provideSheetsClientService.getClient().spreadsheets()?.values()
                        ?.append(sheetId, range, body)
                        ?.setValueInputOption(ValueInputOptions.RAW)
                        ?.execute()

                    GoogleSheetsPostApiResponse.Ok(result)
                } catch (e: Exception) {
                    GoogleSheetsPostApiResponse.Error(e.message)
                }
            }.await()
        }
    }

    private fun List<List<Any>>?.toData(): List<List<String>> {
        return this?.map { row -> row.map { cell -> cell.toString() } } ?: emptyList()
    }
}

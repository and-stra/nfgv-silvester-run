package com.nfgv.stopwatch.data.repository.remote

import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import com.nfgv.stopwatch.data.service.ProvideGoogleSheetsClientService
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsReadDataApiResponse
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsAppendDataApiResponse
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsAddSheetApiResponse
import com.nfgv.stopwatch.data.service.ValueInputOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSheetsRepository @Inject constructor(
    private val provideSheetsClientService: ProvideGoogleSheetsClientService
) {
    suspend fun readValues(sheetId: String, range: String): GoogleSheetsReadDataApiResponse {
        return coroutineScope {
            async {
                return@async try {
                    val result = provideSheetsClientService.getClient().spreadsheets()?.values()
                        ?.get(sheetId, range)
                        ?.execute()
                        ?.getValues()

                    GoogleSheetsReadDataApiResponse.Ok(result.toData())
                } catch (e: Exception) {
                    GoogleSheetsReadDataApiResponse.Error(e.message)
                }
            }.await()
        }
    }

    suspend fun appendValues(
        sheetId: String,
        range: String,
        values: List<List<Any>>
    ): GoogleSheetsAppendDataApiResponse {
        val body = ValueRange().setValues(values).setRange(range)

        return coroutineScope {
            async {
                return@async try {
                    val result = provideSheetsClientService.getClient().spreadsheets()?.values()
                        ?.append(sheetId, range, body)
                        ?.setValueInputOption(ValueInputOptions.RAW)
                        ?.execute()

                    GoogleSheetsAppendDataApiResponse.Ok(result)
                } catch (e: Exception) {
                    GoogleSheetsAppendDataApiResponse.Error(e.message)
                }
            }.await()
        }
    }

    suspend fun addSheet(sheetId: String, title: String): GoogleSheetsAddSheetApiResponse {
        return coroutineScope {
            async {
                return@async try {
                    val properties = SheetProperties().apply {
                        this.title = title
                    }

                    val addSheetRequest = Request().apply {
                        addSheet = AddSheetRequest().setProperties(properties)
                    }

                    val result = provideSheetsClientService.getClient().spreadsheets().batchUpdate(
                        sheetId,
                        BatchUpdateSpreadsheetRequest().setRequests(mutableListOf(addSheetRequest))
                    ).execute()

                    GoogleSheetsAddSheetApiResponse.Ok(result, title)
                } catch (e: Exception) {
                    GoogleSheetsAddSheetApiResponse.Error(e.message)
                }
            }.await()
        }
    }


    private fun List<List<Any>>?.toData(): List<List<String>> {
        return this?.map { row -> row.map { cell -> cell.toString() } } ?: emptyList()
    }
}

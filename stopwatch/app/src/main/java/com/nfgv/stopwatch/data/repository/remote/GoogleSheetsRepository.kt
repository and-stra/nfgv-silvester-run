package com.nfgv.stopwatch.data.repository.remote

import androidx.datastore.preferences.core.stringPreferencesKey
import com.nfgv.stopwatch.data.service.ProvideGoogleSheetsClientService
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsGetApiResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    private fun List<List<Any>>?.toData(): List<List<String>> {
        return this?.map { row -> row.map { cell -> cell.toString() } } ?: emptyList()
    }
}

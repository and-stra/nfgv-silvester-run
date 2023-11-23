package com.nfgv.stopwatch.service.sheets

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes.SPREADSHEETS
import com.google.api.services.sheets.v4.model.ValueRange
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.service.auth.GoogleSignInService

object ValueInputOptions {
    const val RAW = "RAW"
    const val USER_ENTERED = "USER_ENTERED"
}

class GoogleSheetService private constructor() {
    private val googleSignInService = GoogleSignInService.instance

    private var sheetsClient: Sheets? = null

    companion object {
        val instance: GoogleSheetService by lazy {
            GoogleSheetService()
        }
    }

    fun initClient(context: Context) {
        val scopes = listOf(SPREADSHEETS)
        val credential = GoogleAccountCredential.usingOAuth2(context, scopes)
        val account = googleSignInService.getSignedInAccount(context)

        credential.selectedAccount = account?.account

        val jsonFactory = JacksonFactory.getDefaultInstance()
        val httpTransport = NetHttpTransport()

        sheetsClient = Sheets.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(context.resources.getString(R.string.app_name))
            .build()
    }

    fun readValues(sheetId: String, range: String): MutableList<MutableList<Any>>? {
        return sheetsClient?.spreadsheets()?.values()
            ?.get(sheetId, range)
            ?.execute()
            ?.getValues()
    }

    fun appendValues(sheetId: String, range: String, values: List<List<Any>>) {
        val body = ValueRange().setValues(values).setRange(range)

        sheetsClient?.spreadsheets()?.values()
            ?.append(sheetId, range, body)
            ?.setValueInputOption(ValueInputOptions.RAW)
            ?.execute()
    }
}
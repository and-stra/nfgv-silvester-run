package com.nfgv.stopwatch.data.service

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.nfgv.stopwatch.auth.service.FindLoggedInAccountService
import com.nfgv.stopwatch.data.service.exception.NotSignedInException
import com.nfgv.stopwatch.util.extensions.getApplicationName
import javax.inject.Inject
import javax.inject.Singleton

object ValueInputOptions {
    const val RAW = "RAW"
    const val USER_ENTERED = "USER_ENTERED"
}

@Singleton
class ProvideGoogleSheetsClientService @Inject constructor(
    private val context: Context,
    private val findLoggedInAccountService: FindLoggedInAccountService
) {
    private var client: Sheets? = null

    fun getClient(): Sheets {
        return client ?: buildClient()
    }

    private fun buildClient(): Sheets {
        val scopes = listOf(SheetsScopes.SPREADSHEETS)
        val credential = GoogleAccountCredential.usingOAuth2(context, scopes)
        val account = findLoggedInAccountService.findLoggedInAccount()

        credential.selectedAccount = account?.account ?: throw NotSignedInException()

        val jsonFactory = JacksonFactory.getDefaultInstance()
        val httpTransport = NetHttpTransport()

        return Sheets.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(context.getApplicationName())
            .build()
    }
}
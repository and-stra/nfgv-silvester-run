package com.nfgv.stopwatch.auth.service

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindLoggedInAccountService @Inject constructor(private val context: Context) {
    fun findLoggedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}
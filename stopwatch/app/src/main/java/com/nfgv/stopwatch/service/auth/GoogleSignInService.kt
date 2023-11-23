package com.nfgv.stopwatch.service.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope


class GoogleSignInService private constructor() {
    companion object {
        val instance: GoogleSignInService by lazy {
            GoogleSignInService()
        }
    }

    fun isSignedIn(context: Context): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    fun getSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun requestSignIn(context: Context, scope: Scope): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(scope)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, signInOptions)
    }

    fun signIn(data: Intent?) {
        if (data != null) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
        }
    }
}
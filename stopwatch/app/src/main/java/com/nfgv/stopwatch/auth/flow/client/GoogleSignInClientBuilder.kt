package com.nfgv.stopwatch.auth.flow.client

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

class GoogleSignInClientBuilder(private val context: Context) {
    private val signInOptionsBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)

    fun withScopes(scope: Scope, vararg scopes: Scope): GoogleSignInClientBuilder {
        signInOptionsBuilder.requestScopes(scope, *scopes)
        return this
    }

    fun withRequestEmail(): GoogleSignInClientBuilder {
        signInOptionsBuilder.requestEmail()
        return this
    }

    fun build(): GoogleSignInClient {
        val signInOptions = signInOptionsBuilder.build()
        return GoogleSignIn.getClient(context, signInOptions)
    }
}
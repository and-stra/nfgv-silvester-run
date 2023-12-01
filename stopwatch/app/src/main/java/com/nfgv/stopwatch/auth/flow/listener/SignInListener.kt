package com.nfgv.stopwatch.auth.flow.listener

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException

interface SignInListener {
    fun onSignInSuccess(account: GoogleSignInAccount)

    fun onSignInFailure(exception: ApiException)
}
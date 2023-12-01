package com.nfgv.stopwatch.auth.flow

import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.nfgv.stopwatch.auth.flow.contract.SignInActivityResultContract
import com.nfgv.stopwatch.auth.flow.listener.SignInListener

class GoogleSignInFlow(fragment: Fragment) {
    private var signInListener: SignInListener? = null

    private val signInLauncher = fragment.registerForActivityResult(
        SignInActivityResultContract()
    ) { result: Task<GoogleSignInAccount> ->
        handleSignInResult(result)
    }

    fun addSignInListener(listener: SignInListener) {
        signInListener = listener
    }

    fun signInWithClient(googleSignInClient: GoogleSignInClient) {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            signInListener?.onSignInSuccess(account!!)
        } catch (e: ApiException) {
            signInListener?.onSignInFailure(e)
        }
    }
}
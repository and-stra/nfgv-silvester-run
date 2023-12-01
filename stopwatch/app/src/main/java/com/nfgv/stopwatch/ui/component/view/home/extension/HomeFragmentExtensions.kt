package com.nfgv.stopwatch.ui.component.view.home.extension

import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.sheets.v4.SheetsScopes
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.auth.flow.GoogleSignInFlow
import com.nfgv.stopwatch.auth.flow.client.GoogleSignInClientBuilder
import com.nfgv.stopwatch.auth.flow.listener.SignInListener
import com.nfgv.stopwatch.ui.component.custom.ProgressDialog
import com.nfgv.stopwatch.ui.component.view.home.HomeFragment
import com.nfgv.stopwatch.ui.component.view.home.adapter.StopperIdAdapter

fun HomeFragment.setStopperIdAdapter() {
    binding.inputStopperIdAutocompleteView.setAdapter(StopperIdAdapter(requireContext()))
}

fun HomeFragment.createSignInFlow(): GoogleSignInFlow {
    return GoogleSignInFlow(this).apply {
        addSignInListener(object : SignInListener {
            override fun onSignInSuccess(account: GoogleSignInAccount) {
                viewModel.onAccountSignedIn()
            }

            override fun onSignInFailure(exception: ApiException) {
                Toast.makeText(context, R.string.toast_sign_in_failed, Toast.LENGTH_SHORT).show()
            }
        })
    }
}

fun HomeFragment.createProgressDialog(): ProgressDialog {
    return ProgressDialog(requireContext())
}

fun HomeFragment.signInWithGoogleAccount() {
    val signInClient = GoogleSignInClientBuilder(requireContext())
        .withRequestEmail()
        .withScopes(Scope(SheetsScopes.SPREADSHEETS))
        .build()

    signInFlow.signInWithClient(signInClient)
}

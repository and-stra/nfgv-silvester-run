package com.nfgv.stopwatch.ui.component.view.home.extension

import android.os.Bundle
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.sheets.v4.SheetsScopes
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.auth.flow.GoogleSignInFlow
import com.nfgv.stopwatch.auth.flow.client.GoogleSignInClientBuilder
import com.nfgv.stopwatch.auth.flow.listener.SignInListener
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsReadDataApiResponse
import com.nfgv.stopwatch.ui.component.custom.ProgressDialog
import com.nfgv.stopwatch.ui.component.view.home.HomeFragment
import com.nfgv.stopwatch.ui.component.view.home.adapter.StopperIdAdapter
import com.nfgv.stopwatch.util.Constants
import java.lang.Exception

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

fun HomeFragment.navigateToStopwatchFragmentWithRunStartTime(result: GoogleSheetsReadDataApiResponse.Ok) {
    result.toRunStartTime().also {
        navigateToStopwatchFragment(it)
    }
}

fun HomeFragment.navigateToStopwatchFragmentWithoutRunStartTime() {
    navigateToStopwatchFragment(null)
}

private fun HomeFragment.navigateToStopwatchFragment(runStartTime: Long?) {
    val args = Bundle().apply {
        putString(Constants.SHEETS_ID_KEY, viewModel.sheetsId.value)
        putString(Constants.STOPPER_ID_KEY, viewModel.stopperId.value)
        runStartTime?.let { putLong(Constants.RUN_START_TIME_KEY, it) }
    }

    findNavController().navigate(R.id.action_homeFragment_to_stopwatchFragment, args)
}

fun HomeFragment.navigateToAboutFragment() {
    findNavController().navigate(R.id.action_homeFragment_to_aboutFragment)
}

fun GoogleSheetsReadDataApiResponse.Ok.toRunStartTime(): Long? {
    return try {
        data[0][0].toLong()
    } catch (e: Exception) {
        null
    }
}

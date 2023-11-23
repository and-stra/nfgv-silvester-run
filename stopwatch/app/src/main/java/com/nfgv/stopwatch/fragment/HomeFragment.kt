package com.nfgv.stopwatch.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.SignInButton.SIZE_WIDE
import com.google.android.gms.common.api.Scope
import com.google.api.services.sheets.v4.SheetsScopes.SPREADSHEETS
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.component.OnscreenNotification
import com.nfgv.stopwatch.component.ProgressDialog
import com.nfgv.stopwatch.databinding.HomeFragmentBinding
import com.nfgv.stopwatch.service.GoogleSheetService
import com.nfgv.stopwatch.service.GoogleSignInService
import com.nfgv.stopwatch.util.runOnCoroutineThread
import com.nfgv.stopwatch.util.runOnUIThread
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.IndexOutOfBoundsException
import java.lang.NumberFormatException

class HomeFragment : Fragment() {
    private var _binding: HomeFragmentBinding? = null
    private var signInResultLauncher: ActivityResultLauncher<Intent>? = null

    private val binding get() = _binding!!
    private val googleSignInService = GoogleSignInService.instance
    private val googleSheetService = GoogleSheetService.instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        registerSignInResultLauncher()
        registerListeners()
        trySignInExistingAccount()
    }

    override fun onResume() {
        super.onResume()

        initStopperIdDropdownItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun initView() {
        binding.buttonGoogleSignIn.setSize(SIZE_WIDE)
        binding.inputSheetId.editText?.setText("16o6wVeRQO7KFl0bx6Q5P9QgaHrrGbNinyiqwiwvqwR4")
    }

    private fun registerSignInResultLauncher() {
        signInResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                googleSignInService.signIn(result.data)

                onSignIn()
            }
        }
    }

    private fun registerListeners() {
        binding.buttonGo.setOnClickListener { runOnCoroutineThread { connectGoogleSheetsClient() } }
        binding.buttonGoogleSignIn.setOnClickListener { startSignInFlow() }
    }

    private fun trySignInExistingAccount() {
        if (googleSignInService.isSignedIn(requireContext())) {
            onSignIn()
        }
    }

    private suspend fun connectGoogleSheetsClient() {
        val context = requireContext()
        val sheetId = binding.inputSheetId.editText?.text.toString()
        val progressDialog = ProgressDialog(context)

        runOnUIThread { progressDialog.show() }
        googleSheetService.initClient(context)

        try {
            coroutineScope {
                val result = async {
                    googleSheetService.readValues(sheetId, "Laufdaten!B1:B2")
                }.await()

                val runName = result?.get(0)?.get(0).toString()
                val runStartTime = result?.get(1)?.get(0).toString().toLong()

                runOnUIThread { navigateToStopwatchFragment(runName, runStartTime) }
            }
        } catch (e: Exception) {
            when (e) {
                is IndexOutOfBoundsException, is NumberFormatException -> {
                    runOnUIThread { OnscreenNotification(context, R.string.toast_run_data_invalid) }
                }
                else -> {
                    runOnUIThread { OnscreenNotification(context, R.string.toast_connection_failed) }
                }
            }
        } finally {
            runOnUIThread { progressDialog.dismiss() }
        }
    }

    private fun initStopperIdDropdownItems() {
        val stopperIdItems = resources.getStringArray(R.array.home_stopper_id_items)
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, stopperIdItems)

        binding.inputStopperIdAutocompleteView.setAdapter(adapter)
    }

    private fun startSignInFlow() {
        signInResultLauncher?.launch(
            googleSignInService.requestSignIn(
                requireContext(),
                Scope(SPREADSHEETS)
            ).signInIntent
        )
    }

    private fun onSignIn() {
        val signedIn = googleSignInService.isSignedIn(requireContext())

        binding.buttonGoogleSignIn.isVisible = !signedIn
        binding.buttonGo.isVisible = signedIn
    }

    private fun navigateToStopwatchFragment(runName: String?, runStartTime: Long) {
        val args = Bundle().apply {
            putString("sheetId", binding.inputSheetId.editText?.text.toString())
            putString("stopperId", binding.inputStopperId.editText?.text.toString())
            putLong("runStartTime", runStartTime)
            putString("runName", runName)
        }

        findNavController().navigate(R.id.action_HomeFragment_to_StopwatchFragment, args)
    }
}
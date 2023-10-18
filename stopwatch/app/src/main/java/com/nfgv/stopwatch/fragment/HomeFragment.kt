package com.nfgv.stopwatch.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.SignInButton.SIZE_WIDE
import com.google.android.gms.common.api.Scope
import com.google.api.services.sheets.v4.SheetsScopes.SPREADSHEETS
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.databinding.HomeFragmentBinding
import com.nfgv.stopwatch.service.GoogleSignInService

class HomeFragment : Fragment() {
    private var _binding: HomeFragmentBinding? = null

    private val binding get() = _binding!!
    private val googleSignInService = GoogleSignInService.instance
    private var signInResultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerSignInResultLauncher()

        if (googleSignInService.isSignedIn(this.requireContext())) {
            onSignIn()
        }

        binding.buttonGo.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_StopwatchFragment)
        }

        binding.buttonGoogleSignIn.setSize(SIZE_WIDE)
        binding.buttonGoogleSignIn.setOnClickListener { signIn() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun onSignIn() {
        val signedIn = googleSignInService.isSignedIn(this.requireContext())

        binding.buttonGoogleSignIn.isVisible = !signedIn
        binding.buttonGo.isVisible = signedIn
    }

    private fun signIn() {
        signInResultLauncher?.launch(
            googleSignInService.requestSignIn(
                this.requireContext(),
                Scope(SPREADSHEETS)
            ).signInIntent
        )
    }
}
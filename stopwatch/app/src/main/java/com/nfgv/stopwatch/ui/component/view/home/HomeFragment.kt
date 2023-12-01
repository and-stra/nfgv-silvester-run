package com.nfgv.stopwatch.ui.component.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.auth.flow.GoogleSignInFlow
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsGetApiResponse
import com.nfgv.stopwatch.ui.component.custom.ProgressDialog
import com.nfgv.stopwatch.databinding.HomeFragmentBinding
import com.nfgv.stopwatch.ui.component.base.BaseFragment
import com.nfgv.stopwatch.ui.component.view.home.extension.createProgressDialog
import com.nfgv.stopwatch.ui.component.view.home.extension.createSignInFlow
import com.nfgv.stopwatch.ui.component.view.home.extension.setStopperIdAdapter
import com.nfgv.stopwatch.ui.component.view.home.extension.signInWithGoogleAccount
import com.nfgv.stopwatch.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IndexOutOfBoundsException
import java.lang.NumberFormatException

@AndroidEntryPoint
class HomeFragment : BaseFragment() {
    lateinit var binding: HomeFragmentBinding
    lateinit var signInFlow: GoogleSignInFlow
    lateinit var progressDialog: ProgressDialog

    val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        signInFlow = createSignInFlow()
        progressDialog = createProgressDialog()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        initMenu()
        setStopperIdAdapter()

        binding.inputStopperIdAutocompleteView.doAfterTextChanged { text ->
            viewModel.updateStopperId(text.toString())
        }
        binding.inputSheetId.editText?.doAfterTextChanged { text ->
            viewModel.updateSheetsId(text.toString())
        }

        binding.buttonGoogleSignIn.setOnClickListener { signInWithGoogleAccount() }

        binding.buttonGo.setOnClickListener {
            progressDialog.show()
            viewModel.fetchRunData()
        }
    }

    override fun observeViewModel() {
        viewModel.runData.observe(this) { result ->
            when (result) {
                is GoogleSheetsGetApiResponse.Ok -> {
                    val runName = result.data[0][0]
                    val runStartTimestamp = result.data[1][0].toLong()

                    // TODO validate!!!

                    navigateToStopwatchFragment(runName, runStartTimestamp)
                }
                is GoogleSheetsGetApiResponse.Error -> {
                    Toast.makeText(context, R.string.toast_connection_failed, Toast.LENGTH_SHORT).show()
                }
            }

            progressDialog.dismiss()
        }
    }

    private fun initMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_about -> {
                        navigateToAboutFragment()
                        return true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun navigateToStopwatchFragment(runName: String?, runStartTime: Long) {
        val args = Bundle().apply {
            putString(Constants.SHEETS_ID_KEY, viewModel.sheetsId.value)
            putString(Constants.STOPPER_ID_KEY, viewModel.stopperId.value)
            putLong(Constants.RUN_START_TIME_KEY, runStartTime)
            putString(Constants.RUN_NAME_KEY, runName)
        }

        findNavController().navigate(R.id.action_homeFragment_to_stopwatchFragment, args)
    }

    private fun navigateToAboutFragment() {
        findNavController().navigate(R.id.action_homeFragment_to_aboutFragment)
    }
}
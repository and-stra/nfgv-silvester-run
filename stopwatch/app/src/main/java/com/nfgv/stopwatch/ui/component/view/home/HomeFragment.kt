package com.nfgv.stopwatch.ui.component.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.auth.flow.GoogleSignInFlow
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsReadDataApiResponse
import com.nfgv.stopwatch.ui.component.custom.ProgressDialog
import com.nfgv.stopwatch.databinding.HomeFragmentBinding
import com.nfgv.stopwatch.ui.component.base.BaseFragment
import com.nfgv.stopwatch.ui.component.view.home.extension.createProgressDialog
import com.nfgv.stopwatch.ui.component.view.home.extension.createSignInFlow
import com.nfgv.stopwatch.ui.component.view.home.extension.navigateToAboutFragment
import com.nfgv.stopwatch.ui.component.view.home.extension.navigateToStopwatchFragmentWithoutRunStartTime
import com.nfgv.stopwatch.ui.component.view.home.extension.navigateToStopwatchFragmentWithRunStartTime
import com.nfgv.stopwatch.ui.component.view.home.extension.setStopperIdAdapter
import com.nfgv.stopwatch.ui.component.view.home.extension.signInWithGoogleAccount
import dagger.hilt.android.AndroidEntryPoint

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
            viewModel.fetchRunStartTime()
        }
    }

    override fun observeViewModel() {
        viewModel.fetchRunStartTimeResponse.observe(this) { result ->
            when (result) {
                is GoogleSheetsReadDataApiResponse.Ok -> navigateToStopwatchFragmentWithRunStartTime(result)
                is GoogleSheetsReadDataApiResponse.Error -> navigateToStopwatchFragmentWithoutRunStartTime()
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
}
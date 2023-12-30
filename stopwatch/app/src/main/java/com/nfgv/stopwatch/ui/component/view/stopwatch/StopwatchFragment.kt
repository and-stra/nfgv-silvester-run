package com.nfgv.stopwatch.ui.component.view.stopwatch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsAddSheetApiResponse
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsAppendDataApiResponse
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsReadDataApiResponse
import com.nfgv.stopwatch.ui.component.extension.flash
import com.nfgv.stopwatch.ui.component.extension.triggerVibrate
import com.nfgv.stopwatch.databinding.StopwatchFragmentBinding
import com.nfgv.stopwatch.ui.component.base.BaseFragment
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.backupUploadFailed
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.backupUploadSuccessful
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.connect
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.disableScreenTimeout
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.disconnect
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.enableScreenTimeout
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.updateMeasuredTimestampsFrom
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.setActionBarTitle
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.setRunStartTime
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.updateMeasuredTimestampsFromBackup
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.uploadBackupData
import com.nfgv.stopwatch.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StopwatchFragment : BaseFragment() {
    lateinit var binding: StopwatchFragmentBinding

    val viewModel: StopwatchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.stopwatch_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.onViewCreated(
            arguments?.getString(Constants.STOPPER_ID_KEY).orEmpty(),
            arguments?.getString(Constants.SHEETS_ID_KEY).orEmpty(),
            arguments?.getLong(Constants.RUN_START_TIME_KEY)
        )
        disableScreenTimeout()
        setActionBarTitle()
        initMenu()

        binding.buttonStopTime.setOnClickListener {
            viewModel.storeCurrentTimestamp()
            binding.buttonStopTime.flash()
            binding.buttonStopTime.triggerVibrate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.onDestroyView()
        enableScreenTimeout()
    }

    override fun observeViewModel() {
        viewModel.fetchTimestampsResponse.observe(this) { result ->
            when (result) {
                is GoogleSheetsReadDataApiResponse.Ok -> {
                    updateMeasuredTimestampsFrom(result)
                    connect()
                }
                is GoogleSheetsReadDataApiResponse.Error -> {
                    updateMeasuredTimestampsFromBackup()
                    disconnect()
                }
                else -> {}
            }
        }
        viewModel.addBackupSheetResponse.observe(this) { result ->
            when (result) {
                is GoogleSheetsAddSheetApiResponse.Ok -> uploadBackupData(result.sheetTitle)
                is GoogleSheetsAddSheetApiResponse.Error -> disconnect()
                else -> {}
            }
        }
        viewModel.uploadBackupDataResponse.observe(this) { result ->
            when (result) {
                is GoogleSheetsAppendDataApiResponse.Ok -> backupUploadSuccessful()
                is GoogleSheetsAppendDataApiResponse.Error -> backupUploadFailed()
                else -> {}
            }
        }
        viewModel.fetchRunStartTimeResponse.observe(this) { result ->
            when (result) {
                is GoogleSheetsReadDataApiResponse.Ok -> setRunStartTime(result)
                is GoogleSheetsReadDataApiResponse.Error -> disconnect(true)
                else -> {}
            }
        }
    }

    private fun initMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.stopwatch_menu, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_fetch_run_start_time -> {
                        viewModel.fetchRunStartTime()
                        return true
                    }
                    R.id.action_upload_backup -> {
                        viewModel.addBackupSheet()
                        return true
                    }
                    R.id.action_delete_backup -> {
                        viewModel.deleteBackup()
                        return true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}
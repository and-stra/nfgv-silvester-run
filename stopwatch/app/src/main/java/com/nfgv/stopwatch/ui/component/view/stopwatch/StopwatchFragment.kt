package com.nfgv.stopwatch.ui.component.view.stopwatch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsGetApiResponse
import com.nfgv.stopwatch.ui.component.extension.flash
import com.nfgv.stopwatch.ui.component.extension.triggerVibrate
import com.nfgv.stopwatch.databinding.StopwatchFragmentBinding
import com.nfgv.stopwatch.ui.component.base.BaseFragment
import com.nfgv.stopwatch.ui.component.view.stopwatch.adapter.MeasuredTimestampsAdapter
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.disableScreenTimeout
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.enableScreenTimeout
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.setActionBarTitle
import com.nfgv.stopwatch.ui.component.view.stopwatch.extension.setStopTimeButtonBackgroundColor
import com.nfgv.stopwatch.util.Constants
import com.nfgv.stopwatch.util.toHHMMSSs
import dagger.hilt.android.AndroidEntryPoint
import java.lang.StringBuilder

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
            arguments?.getLong(Constants.RUN_START_TIME_KEY) ?: 0L,
        )
        disableScreenTimeout()
        setStopTimeButtonBackgroundColor()
        setActionBarTitle()

        binding.buttonStopTime.setOnClickListener {
            viewModel.queueTimestamp()
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
        viewModel.measuredTimestamps.observe(this) { result ->
            when (result) {
                is GoogleSheetsGetApiResponse.Ok -> {
                    binding.listviewTimeResults.adapter = MeasuredTimestampsAdapter(
                        requireContext(), formatTimestamps(result.data)
                    )
                }
                is GoogleSheetsGetApiResponse.Error -> {
                    // TODO
                }
            }
        }
    }

    private fun formatTimestamps(timestampsRaw: List<List<String>>): List<String> {
        val runStartTimestamp = arguments?.getLong(Constants.RUN_START_TIME_KEY) ?: 0L
        return timestampsRaw.mapIndexed { index, timestamp ->
            formatTimestamp(index, if (timestamp.isNotEmpty()) timestamp[0] else "", runStartTimestamp)
        }
    }

    private fun formatTimestamp(index: Int, timestamp: String, runStartTime: Long): String {
        val result = StringBuilder()
            .append(String.format("%3d", index + 1))
            .append("                  ")

        try {
            val timeElapsed = timestamp.toLong() - runStartTime
            if (timeElapsed > 0) {
                result.append(timeElapsed.toHHMMSSs())
            } else {
                result.append("--:--:--.-")
            }
        } catch (e: NumberFormatException) {
            result.append(result)
        }

        return result.toString()
    }
}
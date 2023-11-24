package com.nfgv.stopwatch.fragment

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.component.OnscreenNotification
import com.nfgv.stopwatch.component.extension.flash
import com.nfgv.stopwatch.component.extension.triggerVibrate
import com.nfgv.stopwatch.databinding.StopwatchFragmentBinding
import com.nfgv.stopwatch.service.sheets.FetchTimeResultsService
import com.nfgv.stopwatch.service.sheets.PublishTimestampService
import com.nfgv.stopwatch.util.Constants
import com.nfgv.stopwatch.util.CyclicTask
import com.nfgv.stopwatch.util.runOnCoroutineThread
import com.nfgv.stopwatch.util.runOnUIThread
import com.nfgv.stopwatch.util.toCET
import com.nfgv.stopwatch.util.toHHMMSS
import com.nfgv.stopwatch.util.toHHMMSSs

class StopwatchFragment : Fragment() {
    private var _binding: StopwatchFragmentBinding? = null

    private val binding get() = _binding!!
    private val publishTimestampService = PublishTimestampService.instance
    private val fetchTimeResultsService = FetchTimeResultsService.instance
    private val stopwatchTimerTask = CyclicTask(50L)
    private val timeResultFetchTask = CyclicTask(1100L)
    private val timestampPublishTask = CyclicTask(1100L)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StopwatchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        startBackgroundTasks()
        registerListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        destroyView()
        stopwatchTimerTask.stop()
        timeResultFetchTask.stop()
        timestampPublishTask.cancel()
        _binding = null
    }

    private fun initView() {
        // set action bar title
        (activity as AppCompatActivity).supportActionBar?.title =
            arguments?.getString(Constants.RUN_NAME_KEY).orEmpty()

        // disable screen timeout
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // set view background color
        val currentTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentTheme == Configuration.UI_MODE_NIGHT_YES) {
            binding.buttonStopTime.setBackgroundColor(Color.WHITE)
        } else {
            binding.buttonStopTime.setBackgroundColor(Color.BLACK)
        }

        binding.textStopperId.text = arguments?.getString(Constants.STOPPER_ID_KEY).orEmpty()
    }

    private fun startBackgroundTasks() {
        val sheetId = arguments?.getString(Constants.SHEET_ID_KEY).orEmpty()
        val stopperId = arguments?.getString(Constants.STOPPER_ID_KEY).orEmpty()
        val runStartTime = arguments?.getLong(Constants.RUN_START_TIME_KEY) ?: 0L

        stopwatchTimerTask.start { updateStopwatchTime(runStartTime) }
        timeResultFetchTask.start { fetchTimeResults(sheetId) }
        timestampPublishTask.start { publishTimestampService.publish(sheetId, stopperId) }
    }

    private fun registerListeners() {
        binding.buttonStopTime.setOnClickListener {
            runOnCoroutineThread { stopTime() }
            binding.buttonStopTime.flash()
            binding.buttonStopTime.triggerVibrate()
        }
    }

    private fun destroyView() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun updateStopwatchTime(runStartTime: Long) {
        val timeElapsed = System.currentTimeMillis() - runStartTime
        var text = "${R.string.text_start_time} ${runStartTime.toCET().toHHMMSS()}"

        if (timeElapsed > 0) {
            text = timeElapsed.toHHMMSSs()
        }

        runOnUIThread {
            if (_binding != null) {
                binding.textStopwatch.text = text
            }
        }
    }

    private fun stopTime() {
        try {
            publishTimestampService.queue(System.currentTimeMillis())
        } catch (e: Exception) {
            runOnUIThread {
                OnscreenNotification(requireContext(), R.string.toast_connection_failed)
            }
        }
    }

    private fun fetchTimeResults(sheetId: String) {
        try {
            runOnCoroutineThread {
                updateTimeResults(fetchTimeResultsService.fetch(sheetId))
            }
        } catch (e: Exception) {
            updateTimeResults(emptyArray())
        }
    }

    private fun updateTimeResults(results: Array<String>) {
        runOnUIThread {
            if (context != null) {
                val adapter = ArrayAdapter(requireContext(), R.layout.listview_item, results)
                binding.listviewTimeResults.adapter = adapter
            }
        }
    }
}
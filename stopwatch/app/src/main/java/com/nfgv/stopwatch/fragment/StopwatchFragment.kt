package com.nfgv.stopwatch.fragment

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.component.extension.flash
import com.nfgv.stopwatch.component.extension.triggerVibrate
import com.nfgv.stopwatch.databinding.StopwatchFragmentBinding
import com.nfgv.stopwatch.service.persistence.InternalStorageService
import com.nfgv.stopwatch.service.sheets.FetchTimeResultsService
import com.nfgv.stopwatch.service.sheets.PublishTimestampService
import com.nfgv.stopwatch.util.Constants
import com.nfgv.stopwatch.util.CyclicTask
import com.nfgv.stopwatch.util.runOnCoroutineThread
import com.nfgv.stopwatch.util.runOnUIThread
import com.nfgv.stopwatch.util.toCET
import com.nfgv.stopwatch.util.toHHMMSS
import com.nfgv.stopwatch.util.toHHMMSSs
import java.lang.StringBuilder

class StopwatchFragment : Fragment() {
    private var _binding: StopwatchFragmentBinding? = null

    private val binding get() = _binding!!
    private val publishTimestampService = PublishTimestampService.instance
    private val fetchTimeResultsService = FetchTimeResultsService.instance
    private val internalStorageService = InternalStorageService.instance
    private val stopwatchTimerTask = CyclicTask(50L)
    private val timeResultFetchTask = CyclicTask(1200L)
    private val timestampPublishTask = CyclicTask(1200L)
    private val backupTimestamps = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StopwatchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMenu()
        initView()
        readBackupTimestamps()
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

    private fun initMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.stopwatch_menu, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_delete_backup -> {
                        internalStorageService.fileExists(requireContext(), Constants.BACKUP_FILE_NAME_PREFIX)
                        return true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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

    private fun readBackupTimestamps() {
        val fileName = Constants.BACKUP_FILE_NAME_PREFIX + arguments?.getString(
            Constants.SHEET_ID_KEY
        ).orEmpty()

        try {
            val backupRaw = internalStorageService.readFile(requireContext(), fileName)

            backupTimestamps.addAll(backupRaw.split("\n"))
            binding.textBackup.text = resources.getString(R.string.backup_present)
        } catch (e: Exception) {
            binding.textBackup.text = resources.getString(R.string.backup_not_present)
            Log.w("StopWatch", "Failed to read $fileName $e")
        }
    }

    private fun startBackgroundTasks() {
        val sheetId = arguments?.getString(Constants.SHEET_ID_KEY).orEmpty()
        val stopperId = arguments?.getString(Constants.STOPPER_ID_KEY).orEmpty()
        val runStartTime = arguments?.getLong(Constants.RUN_START_TIME_KEY) ?: 0L

        stopwatchTimerTask.start { updateStopwatchTime(runStartTime) }
        timeResultFetchTask.start { fetchTimeResults(sheetId, stopperId, runStartTime) }
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
        var text = "${resources.getString(R.string.text_start_time)} ${runStartTime.toCET().toHHMMSS()}"

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
        val timestamp = System.currentTimeMillis()
        val sheetId = arguments?.getString(Constants.SHEET_ID_KEY).orEmpty()

        internalStorageService.appendToFile(
            requireContext(),
            Constants.BACKUP_FILE_NAME_PREFIX + sheetId,
            "${timestamp}\n"
        )
        try {
            publishTimestampService.queue(timestamp)
        } catch (e: Exception) {
            Log.e("StopWatch", "Failed to publish timestamp $e")
        }
    }

    private fun fetchTimeResults(sheetId: String, stopperId: String, runStartTime: Long) {
        runOnCoroutineThread {
            try {
                updateTimeResults(fetchTimeResultsService.fetch(sheetId, stopperId), runStartTime)
            } catch (e: Exception) {
                Log.e("StopWatch", "Failed to fetch time results $e")
                updateTimeResults(emptyArray())
            }
        }
    }

    private fun updateTimeResults(resultsRaw: Array<String>, runStartTime: Long = 0L) {
        val results = resultsRaw.mapIndexed { index, timestamp ->
            formatTimeResult(index, timestamp, runStartTime)
        }

        runOnUIThread {
            if (context != null) {
                val adapter = ArrayAdapter(requireContext(), R.layout.listview_item, results)
                binding.listviewTimeResults.adapter = adapter
            }
        }
    }

    private fun formatTimeResult(index: Int, timestamp: String, runStartTime: Long): String {
        val result = StringBuilder()
            .append(String.format("%3d", index + 1))
            .append("                  ")

        try {
            val timeElapsed = timestamp.toLong() - runStartTime
            result.append(timeElapsed.toHHMMSSs())
        } catch (e: NumberFormatException) {
            result.append(result)
        }

        return result.toString()
    }
}
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
import com.nfgv.stopwatch.service.GoogleSheetService
import com.nfgv.stopwatch.util.CyclicTask
import com.nfgv.stopwatch.util.runOnCoroutineThread
import com.nfgv.stopwatch.util.runOnUIThread
import com.nfgv.stopwatch.util.toCET
import com.nfgv.stopwatch.util.toHHMMSS
import com.nfgv.stopwatch.util.toHHMMSSs
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class StopwatchFragment : Fragment() {
    private var _binding: StopwatchFragmentBinding? = null

    private val binding get() = _binding!!
    private val googleSheetService = GoogleSheetService.instance
    private val stopwatchTimerTask = CyclicTask(100L)
    private val timeResultFetchTask = CyclicTask(1200L)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StopwatchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sheetId = arguments?.getString("sheetId").orEmpty()
        val stopperId = arguments?.getString("stopperId").orEmpty()
        val runName = arguments?.getString("runName").orEmpty()
        val runStartTime = arguments?.getLong("runStartTime") ?: 0L

        (activity as AppCompatActivity).supportActionBar?.title = runName

        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        stopwatchTimerTask.start { updateTime(runStartTime) }
        timeResultFetchTask.start { fetchTimeResults(sheetId) }

        val currentTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentTheme == Configuration.UI_MODE_NIGHT_YES) {
            binding.buttonStopTime.setBackgroundColor(Color.WHITE)
        } else {
            binding.buttonStopTime.setBackgroundColor(Color.BLACK)
        }

        binding.buttonStopTime.setOnClickListener {
            runOnCoroutineThread { stopTime(sheetId, stopperId) }
            binding.buttonStopTime.flash()
            binding.buttonStopTime.triggerVibrate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        stopwatchTimerTask.stop()
        timeResultFetchTask.stop()
        _binding = null
    }

    private fun updateTime(runStartTime: Long) {
        val timeElapsed = System.currentTimeMillis() - runStartTime
        var text = "Startzeit: ${runStartTime.toCET().toHHMMSS()}"

        if (timeElapsed > 0) {
            text = timeElapsed.toHHMMSSs()
        }

        runOnUIThread {
            if (_binding != null) {
                binding.textStopwatch.text = text
            }
        }
    }

    private suspend fun stopTime(sheetId: String, stopperId: String) {
        try {
            coroutineScope {
                googleSheetService.appendValues(
                    sheetId, "$stopperId!A:A", listOf(listOf(System.currentTimeMillis()))
                )
            }
        } catch (e: Exception) {
            runOnUIThread {
                OnscreenNotification(requireContext(), R.string.toast_connection_failed)
            }
        }
    }

    private fun fetchTimeResults(sheetId: String) {
        runOnCoroutineThread {
            try {
                coroutineScope {
                    val result = async {
                        googleSheetService.readValues(sheetId, "Laufzeiten!C:C")
                    }.await()

                    val items = result?.subList(1, result.size)?.mapIndexed { index, sublist ->
                        "${index + 1}    ${sublist[0]}"
                    }?.toTypedArray() ?: emptyArray()

                    updateTimeResults(items)
                }
            } catch (e: Exception) {
                updateTimeResults(emptyArray())
            }
        }
    }

    private fun updateTimeResults(items: Array<String>) {
        runOnUIThread {
            if (context != null) {
                val adapter = ArrayAdapter(requireContext(), R.layout.listview_item, items)
                binding.listviewTimeResults.adapter = adapter
            }
        }
    }
}
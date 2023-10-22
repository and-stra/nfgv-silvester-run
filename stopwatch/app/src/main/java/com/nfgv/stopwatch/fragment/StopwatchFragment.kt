package com.nfgv.stopwatch.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.component.OnscreenNotification
import com.nfgv.stopwatch.databinding.StopwatchFragmentBinding
import com.nfgv.stopwatch.service.GoogleSheetService
import com.nfgv.stopwatch.util.runOnCoroutineThread
import com.nfgv.stopwatch.util.runOnUIThread
import kotlinx.coroutines.coroutineScope

class StopwatchFragment : Fragment() {
    private var _binding: StopwatchFragmentBinding? = null

    private val binding get() = _binding!!
    private val googleSheetService = GoogleSheetService.instance

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

        (activity as AppCompatActivity).supportActionBar?.title = runName

        binding.buttonStopLeft.setOnClickListener {
            runOnCoroutineThread { stopTime(sheetId, stopperId) }

            binding.buttonStopLeft.isEnabled = false
            binding.buttonStopRight.isEnabled = true
        }

        binding.buttonStopRight.setOnClickListener {
            runOnCoroutineThread { stopTime(sheetId, stopperId) }

            binding.buttonStopRight.isEnabled = false
            binding.buttonStopLeft.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                OnscreenNotification(this.requireContext(), R.string.toast_connection_failed)
            }
        }
    }
}
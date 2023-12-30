package com.nfgv.stopwatch.ui.component.view.stopwatch.extension

import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsReadDataApiResponse
import com.nfgv.stopwatch.ui.component.view.stopwatch.StopwatchFragment
import com.nfgv.stopwatch.ui.component.view.stopwatch.adapter.MeasuredTimestampsAdapter
import com.nfgv.stopwatch.util.toHHMMSSs
import java.lang.Exception

fun StopwatchFragment.disableScreenTimeout() {
    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

fun StopwatchFragment.enableScreenTimeout() {
    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

fun StopwatchFragment.setActionBarTitle() {
    (activity as AppCompatActivity).supportActionBar?.title = viewModel.stopperId.value.orEmpty()
}

fun StopwatchFragment.updateMeasuredTimestampsFrom(result: GoogleSheetsReadDataApiResponse.Ok) {
    updateMeasuredTimestampsFrom(result.data)
}

fun StopwatchFragment.updateMeasuredTimestampsFromBackup() {
    updateMeasuredTimestampsFrom(
        viewModel.backupTimestamps.value?.map { listOf(it.toString()) } ?: emptyList()
    )
}

private fun StopwatchFragment.updateMeasuredTimestampsFrom(timestamps: List<List<String>>) {
    val adapter = (binding.listviewTimeResults.adapter as? MeasuredTimestampsAdapter)
        ?: MeasuredTimestampsAdapter(requireContext(), mutableListOf())

    adapter.updateData(formatTimestamps(timestamps))
    binding.listviewTimeResults.adapter = adapter
}

fun StopwatchFragment.uploadBackupData(sheetTitle: String) {
    viewModel.uploadBackupData(sheetTitle)
}

fun StopwatchFragment.backupUploadSuccessful() {
    Toast.makeText(context, R.string.toast_backup_upload_done, Toast.LENGTH_SHORT).show()
}

fun StopwatchFragment.backupUploadFailed() {
    Toast.makeText(context, R.string.toast_backup_upload_failed, Toast.LENGTH_SHORT).show()
}

fun StopwatchFragment.connect() = viewModel.connect()

fun StopwatchFragment.disconnect(forceShowToast: Boolean = false) {
    if (viewModel.isConnected.value == true || forceShowToast) {
        Toast.makeText(context, R.string.toast_connection_failed, Toast.LENGTH_SHORT).show()
    }

    viewModel.disconnect()
}

fun StopwatchFragment.setRunStartTime(result: GoogleSheetsReadDataApiResponse.Ok) {
    viewModel.setRunStartData(result.toRunStartTime())

    Toast.makeText(context, R.string.toast_fetch_run_start_time_done, Toast.LENGTH_SHORT).show()
}

fun StopwatchFragment.formatTimestamps(timestampsRaw: List<List<String>>): List<String> {
    val referenceTime = viewModel.runStartTime.value ?: 0L

    return timestampsRaw.mapIndexed { index, timestamp ->
        val timeElapsed = try {
            (timestamp.firstOrNull()?.toLongOrNull() ?: 0L) - referenceTime
        } catch (e: NumberFormatException) {
            0L
        }

        val formattedTime = if (timeElapsed > 0) timeElapsed.toHHMMSSs() else "--:--:--.-"
        "%3d                  %s".format(index + 1, formattedTime)
    }
}

private fun GoogleSheetsReadDataApiResponse.Ok.toRunStartTime(): Long? {
    return try {
        data[0][0].toLong()
    } catch (e: Exception) {
        null
    }
}
package com.nfgv.stopwatch.ui.component.view.stopwatch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsGetApiResponse
import com.nfgv.stopwatch.data.service.FetchTimestampsService
import com.nfgv.stopwatch.data.service.PublishTimestampsService
import com.nfgv.stopwatch.ui.component.view.stopwatch.task.FetchTimestampsTask
import com.nfgv.stopwatch.ui.component.view.stopwatch.task.PublishTimestampTask
import com.nfgv.stopwatch.ui.component.view.stopwatch.task.StopWatchTask
import com.nfgv.stopwatch.util.toCET
import com.nfgv.stopwatch.util.toHHMMSS
import com.nfgv.stopwatch.util.toHHMMSSs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DataSource {
    LOCAL,
    REMOTE
}

@HiltViewModel
class StopwatchViewModel @Inject constructor(
    private val publishTimestampsService: PublishTimestampsService,
    private val fetchTimestampsService: FetchTimestampsService
) : ViewModel() {
    private lateinit var publishTimestampTask: PublishTimestampTask
    private lateinit var fetchTimestampsTask: FetchTimestampsTask
    private lateinit var stopWatchTask: StopWatchTask

    private val _stopperId = MutableLiveData("")
    private val _stopwatchTime = MutableLiveData("")
    private val _isBackupPresent = MutableLiveData(false)
    private val _isConnected = MutableLiveData(true)
    private val _dataSource = MutableLiveData(DataSource.REMOTE)
    private val _measuredTimestamps = MutableLiveData<GoogleSheetsGetApiResponse>()

    val stopwatchTime: LiveData<String> get() = _stopwatchTime
    val stopperId: LiveData<String> get() = _stopperId
    val isBackupPresent: LiveData<Boolean> get() = _isBackupPresent
    val isConnected: LiveData<Boolean> get() = _isConnected
    val dataSource: LiveData<DataSource> get() = _dataSource
    val measuredTimestamps: LiveData<GoogleSheetsGetApiResponse> get() = _measuredTimestamps

    init {
        _isBackupPresent.value = false
        _isConnected.value = true
        _dataSource.value = DataSource.REMOTE
    }

    fun onViewCreated(stopperId: String, sheetsId: String, runStartTimestamp: Long) {
        _stopperId.value = stopperId

        publishTimestampTask = PublishTimestampTask(publishTimestampsService, sheetsId, stopperId).also {
            it.start()
        }
        stopWatchTask = StopWatchTask(runStartTimestamp).also {
            it.start()

            viewModelScope.launch {
                it.resultFlow.collect {
                    val timeElapsed = System.currentTimeMillis() - runStartTimestamp
                    if (timeElapsed > 0) {
                        _stopwatchTime.value = timeElapsed.toHHMMSSs()
                    } else {
                        _stopwatchTime.value = "Startzeit: ${runStartTimestamp.toCET().toHHMMSS()}"
                    }
                }
            }
        }
        fetchTimestampsTask = FetchTimestampsTask(
            fetchTimestampsService,
            sheetsId,
            stopperId
        ).also {
            it.start()

            viewModelScope.launch {
                it.resultFlow.collect { measuredTimestamps ->
                    _measuredTimestamps.value = measuredTimestamps
                }
            }
        }
    }

    fun queueTimestamp() {
        publishTimestampTask.queue(System.currentTimeMillis())
    }

    fun onDestroyView() {
        publishTimestampTask.stop()
        fetchTimestampsTask.stop()
        stopWatchTask.stop()
    }
}
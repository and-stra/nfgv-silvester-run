package com.nfgv.stopwatch.ui.component.view.stopwatch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsAddSheetApiResponse
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsAppendDataApiResponse
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsReadDataApiResponse
import com.nfgv.stopwatch.data.service.AddSheetService
import com.nfgv.stopwatch.data.service.BackupTimestampsService
import com.nfgv.stopwatch.data.service.FetchRunStartTimeService
import com.nfgv.stopwatch.data.service.FetchTimestampsService
import com.nfgv.stopwatch.data.service.PublishTimestampsService
import com.nfgv.stopwatch.data.service.UploadBackupDataService
import com.nfgv.stopwatch.ui.component.view.stopwatch.task.FetchTimestampsTask
import com.nfgv.stopwatch.ui.component.view.stopwatch.task.PublishTimestampTask
import com.nfgv.stopwatch.ui.component.view.stopwatch.task.StopWatchTask
import com.nfgv.stopwatch.util.Constants
import com.nfgv.stopwatch.util.toHHMMSSs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class StopwatchViewModel @Inject constructor(
    private val publishTimestampsService: PublishTimestampsService,
    private val backupTimestampsService: BackupTimestampsService,
    private val fetchTimestampsService: FetchTimestampsService,
    private val addSheetService: AddSheetService,
    private val uploadBackupDataService: UploadBackupDataService,
    private val fetchRunStartTimeService: FetchRunStartTimeService
) : ViewModel() {
    private lateinit var publishTimestampTask: PublishTimestampTask
    private lateinit var fetchTimestampsTask: FetchTimestampsTask
    private lateinit var stopWatchTask: StopWatchTask

    private val _stopperId = MutableLiveData("")
    private val _sheetsId = MutableLiveData("")
    private val _runStartTime = MutableLiveData(0L)
    private val _stopwatchTime = MutableLiveData("")
    private val _isBackupPresent = MutableLiveData(false)
    private val _isConnected = MutableLiveData(true)
    private val _fetchTimestampsResponse = MutableLiveData<GoogleSheetsReadDataApiResponse?>()
    private val _addBackupSheetResponse = MutableLiveData<GoogleSheetsAddSheetApiResponse?>()
    private val _uploadBackupDataResponse = MutableLiveData<GoogleSheetsAppendDataApiResponse?>()
    private val _fetchRunStartTimeResponse = MutableLiveData<GoogleSheetsReadDataApiResponse?>()
    private val _backupTimestamps = MutableLiveData<MutableList<Long>>()

    val stopwatchTime: LiveData<String> get() = _stopwatchTime
    val stopperId: LiveData<String> get() = _stopperId
    val runStartTime: LiveData<Long> get() = _runStartTime
    val isBackupPresent: LiveData<Boolean> get() = _isBackupPresent
    val isConnected: LiveData<Boolean> get() = _isConnected
    val fetchTimestampsResponse: LiveData<GoogleSheetsReadDataApiResponse?> get() = _fetchTimestampsResponse
    val addBackupSheetResponse: LiveData<GoogleSheetsAddSheetApiResponse?> get() = _addBackupSheetResponse
    val uploadBackupDataResponse: LiveData<GoogleSheetsAppendDataApiResponse?> get() = _uploadBackupDataResponse
    val fetchRunStartTimeResponse: LiveData<GoogleSheetsReadDataApiResponse?> get() = _fetchRunStartTimeResponse
    val backupTimestamps: LiveData<MutableList<Long>> get() = _backupTimestamps

    fun onViewCreated(stopperId: String, sheetsId: String, runStartTime: Long?) {
        _stopperId.value = stopperId
        _sheetsId.value = sheetsId
        _runStartTime.value = runStartTime ?: currentStartOfDayMillis()
        _isConnected.value = runStartTime != null
        _backupTimestamps.value = backupTimestampsService.read().toMutableList()
        _isBackupPresent.value = _backupTimestamps.value?.isNotEmpty() ?: false

        publishTimestampTask = PublishTimestampTask(
            publishTimestampsService,
            sheetsId,
            stopperId
        ).also {
            it.start()
        }
        stopWatchTask = StopWatchTask().also {
            it.start()

            viewModelScope.launch {
                it.resultFlow.collect {
                    val referenceTime = _runStartTime.value ?: currentStartOfDayMillis()
                    val timeElapsed = System.currentTimeMillis() - referenceTime

                    _stopwatchTime.value = timeElapsed.toHHMMSSs()
                }
            }
        }
        fetchTimestampsTask = FetchTimestampsTask(fetchTimestampsService, sheetsId, stopperId).also {
            it.start()

            viewModelScope.launch {
                it.resultFlow.collect { fetchedTimestampsResponse ->
                    _fetchTimestampsResponse.value = fetchedTimestampsResponse
                }
            }
        }
    }

    fun fetchRunStartTime() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                fetchRunStartTimeService.fetch(_sheetsId.value.orEmpty())
            }.also { result -> _fetchRunStartTimeResponse.value = result }
        }
    }

    fun setRunStartData(runStartTime: Long?) {
        _runStartTime.value = runStartTime
    }

    private fun currentStartOfDayMillis(): Long {
        return LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .atZone(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }

    fun storeCurrentTimestamp() {
        val currentTimestamp = System.currentTimeMillis()

        queueTimestamp(currentTimestamp)
        backupTimestamp(currentTimestamp)
        updateBackupStatus()
    }

    private fun queueTimestamp(timestamp: Long) = publishTimestampTask.queue(timestamp)

    private fun backupTimestamp(timestamp: Long) {
        _backupTimestamps.value?.add(timestamp)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                backupTimestampsService.store(backupTimestamps.value?.toList() ?: emptyList())
            }
        }
    }

    private fun updateBackupStatus() {
        _isBackupPresent.value = _backupTimestamps.value?.isNotEmpty() ?: false
    }

    fun connect() {
        _isConnected.value = true
    }

    fun disconnect() {
        _isConnected.value = false
    }

    fun addBackupSheet() {
        val title = StringBuilder()
            .append(_stopperId.value.orEmpty().replace(" ", "_"))
            .append("_${Constants.BACKUP_SHEET_NAME_PREFIX}")
            .append("_${System.currentTimeMillis()}")
            .toString()

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                addSheetService.add(_sheetsId.value.orEmpty(), title)
            }.also { result -> _addBackupSheetResponse.value = result }
        }
    }

    fun uploadBackupData(sheetTitle: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                uploadBackupDataService.upload(
                    _sheetsId.value.orEmpty(),
                    sheetTitle,
                    _backupTimestamps.value?.map { listOf(it) } ?: emptyList()
                )
            }.also { result -> _uploadBackupDataResponse.value = result }
        }
    }

    fun deleteBackup() {
        backupTimestampsService.deleteAll()

        _isBackupPresent.value = false
        _backupTimestamps.value?.clear()
    }

    fun onDestroyView() {
        _fetchTimestampsResponse.value = null
        _addBackupSheetResponse.value = null
        _uploadBackupDataResponse.value = null
        _fetchRunStartTimeResponse.value = null

        publishTimestampTask.cancel()
        fetchTimestampsTask.stop()
        stopWatchTask.stop()
    }
}
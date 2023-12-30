package com.nfgv.stopwatch.ui.component.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfgv.stopwatch.auth.service.FindLoggedInAccountService
import com.nfgv.stopwatch.data.domain.response.GoogleSheetsReadDataApiResponse
import com.nfgv.stopwatch.data.repository.local.PreferencesRepository
import com.nfgv.stopwatch.data.service.FetchRunStartTimeService
import com.nfgv.stopwatch.util.Constants
import com.nfgv.stopwatch.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val findLoggedInAccountService: FindLoggedInAccountService,
    private val fetchRunStartTimeService: FetchRunStartTimeService
) : ViewModel() {
    private val _stopperId = MutableLiveData("")
    private val _sheetsId = MutableLiveData("")
    private val _isSignedIn = MutableLiveData(false)
    private val _fetchRunStartTimeResponse = SingleLiveEvent<GoogleSheetsReadDataApiResponse>()

    val stopperId: LiveData<String> get() = _stopperId
    val sheetsId: LiveData<String> get() = _sheetsId
    val isSignedIn: LiveData<Boolean> get() = _isSignedIn
    val fetchRunStartTimeResponse: LiveData<GoogleSheetsReadDataApiResponse> get() = _fetchRunStartTimeResponse

    init {
        _isSignedIn.value = findLoggedInAccountService.findLoggedInAccount() != null

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesRepository.readString(Constants.STOPPER_ID_KEY)
            }.also { result -> _stopperId.value = result }

            withContext(Dispatchers.IO) {
                preferencesRepository.readString(Constants.SHEETS_ID_KEY)
            }.also { result -> _sheetsId.value = result }
        }
    }

    fun onAccountSignedIn() {
        _isSignedIn.value = findLoggedInAccountService.findLoggedInAccount() != null
    }

    fun fetchRunStartTime() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                fetchRunStartTimeService.fetch(sheetsId.value.orEmpty())
            }.also { result -> _fetchRunStartTimeResponse.value = result }
        }
    }

    fun updateStopperId(stopperId: String) {
        _stopperId.value = stopperId
        storeStringPreference(Constants.STOPPER_ID_KEY, stopperId)
    }

    fun updateSheetsId(sheetsId: String) {
        _sheetsId.value = sheetsId
        storeStringPreference(Constants.SHEETS_ID_KEY, sheetsId)
    }

    private fun storeStringPreference(key: String, value: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { preferencesRepository.writeString(key, value) }
        }
    }
}
package com.ashkite.pictureclassification.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ashkite.pictureclassification.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.get(application)
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val mediaCount = database.mediaDao().count()
            val scanState = database.scanStateDao().get()
            _state.value = HomeUiState(
                mediaCount = mediaCount,
                lastScanEpoch = scanState?.lastScanEpoch,
                lastSuccessEpoch = scanState?.lastSuccessEpoch,
                errorCount = scanState?.errorCount ?: 0
            )
        }
    }
}

data class HomeUiState(
    val mediaCount: Int = 0,
    val lastScanEpoch: Long? = null,
    val lastSuccessEpoch: Long? = null,
    val errorCount: Int = 0
)

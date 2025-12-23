package com.ashkite.pictureclassification.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ashkite.pictureclassification.data.db.AppDatabase
import com.ashkite.pictureclassification.data.model.DateCount
import com.ashkite.pictureclassification.data.model.PlaceCount
import com.ashkite.pictureclassification.data.model.TagCount
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
            val mediaDao = database.mediaDao()
            val tagDao = database.tagDao()
            val mediaCount = mediaDao.count()
            val scanState = database.scanStateDao().get()
            val placeCounts = mediaDao.getPlaceCounts(LIST_LIMIT)
            val dateCounts = mediaDao.getDateCounts(LIST_LIMIT)
            val unknownTotal = mediaDao.countLocationUnknown()
            val unknownDateCounts = mediaDao.getUnknownDateCounts(LIST_LIMIT)
            val peopleCounts = tagDao.getTagCounts(TAG_TYPE_PEOPLE, LIST_LIMIT)
            val eventCounts = tagDao.getTagCounts(TAG_TYPE_EVENTS, LIST_LIMIT)

            _state.value = HomeUiState(
                mediaCount = mediaCount,
                lastScanEpoch = scanState?.lastScanEpoch,
                lastSuccessEpoch = scanState?.lastSuccessEpoch,
                errorCount = scanState?.errorCount ?: 0,
                placeCounts = placeCounts,
                dateCounts = dateCounts,
                unknownTotal = unknownTotal,
                unknownDateCounts = unknownDateCounts,
                peopleCounts = peopleCounts,
                eventCounts = eventCounts
            )
        }
    }

    companion object {
        private const val LIST_LIMIT = 6
        private const val TAG_TYPE_PEOPLE = "people"
        private const val TAG_TYPE_EVENTS = "event"
    }
}

data class HomeUiState(
    val mediaCount: Int = 0,
    val lastScanEpoch: Long? = null,
    val lastSuccessEpoch: Long? = null,
    val errorCount: Int = 0,
    val placeCounts: List<PlaceCount> = emptyList(),
    val dateCounts: List<DateCount> = emptyList(),
    val unknownTotal: Int = 0,
    val unknownDateCounts: List<DateCount> = emptyList(),
    val peopleCounts: List<TagCount> = emptyList(),
    val eventCounts: List<TagCount> = emptyList()
)

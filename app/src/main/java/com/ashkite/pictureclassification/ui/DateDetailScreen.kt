package com.ashkite.pictureclassification.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ashkite.pictureclassification.R
import com.ashkite.pictureclassification.data.db.AppDatabase
import com.ashkite.pictureclassification.data.model.MediaItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DateDetailScreen(localDate: String, onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    var items by remember { mutableStateOf<List<MediaItemEntity>>(emptyList()) }

    LaunchedEffect(localDate) {
        withContext(Dispatchers.IO) {
            val database = AppDatabase.get(context)
            items = database.mediaDao().getMediaByDate(localDate, MEDIA_LIMIT)
        }
    }

    MediaGridScreen(
        title = localDate,
        subtitle = stringResource(R.string.detail_date_subtitle),
        items = items,
        onBack = onBack
    )
}

private const val MEDIA_LIMIT = 500

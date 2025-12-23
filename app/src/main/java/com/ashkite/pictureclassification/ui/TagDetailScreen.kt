package com.ashkite.pictureclassification.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.ashkite.pictureclassification.data.db.AppDatabase
import com.ashkite.pictureclassification.data.model.MediaItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TagDetailScreen(tagType: String, tagId: Long, onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    var title by remember { mutableStateOf("Tag") }
    var subtitle by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<MediaItemEntity>>(emptyList()) }

    LaunchedEffect(tagId, tagType) {
        withContext(Dispatchers.IO) {
            val database = AppDatabase.get(context)
            val tag = database.tagDao().findById(tagId)
            title = tag?.name ?: "Tag"
            subtitle = when (tagType) {
                "people" -> "People"
                "event" -> "Event"
                else -> tagType
            }
            items = database.mediaDao().getMediaByTag(tagId, MEDIA_LIMIT)
        }
    }

    MediaGridScreen(
        title = title,
        subtitle = subtitle,
        items = items,
        onBack = onBack
    )
}

private const val MEDIA_LIMIT = 500

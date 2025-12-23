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
import com.ashkite.pictureclassification.data.model.TagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TagDetailScreen(tagType: String, tagId: Long, onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    var tag by remember { mutableStateOf<TagEntity?>(null) }
    var items by remember { mutableStateOf<List<MediaItemEntity>>(emptyList()) }

    LaunchedEffect(tagId, tagType) {
        withContext(Dispatchers.IO) {
            val database = AppDatabase.get(context)
            tag = database.tagDao().findById(tagId)
            items = database.mediaDao().getMediaByTag(tagId, MEDIA_LIMIT)
        }
    }

    val title = tag?.let { displayTagName(tagType, it.name) }
        ?: stringResource(R.string.detail_tag_default)
    val subtitle = when (tagType) {
        "people" -> stringResource(R.string.detail_tag_type_people)
        "event" -> stringResource(R.string.detail_tag_type_event)
        else -> tagType
    }

    MediaGridScreen(
        title = title,
        subtitle = subtitle,
        items = items,
        onBack = onBack
    )
}

private const val MEDIA_LIMIT = 500

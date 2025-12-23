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
fun PlaceDetailScreen(cityId: Long, onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    var title by remember { mutableStateOf("Place") }
    var subtitle by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<MediaItemEntity>>(emptyList()) }

    LaunchedEffect(cityId) {
        withContext(Dispatchers.IO) {
            val database = AppDatabase.get(context)
            val city = database.cityDao().findById(cityId)
            title = city?.nameKo?.ifBlank { city.nameEn } ?: city?.nameEn ?: "Unknown"
            subtitle = city?.countryCode
            items = database.mediaDao().getMediaByCity(cityId, MEDIA_LIMIT)
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

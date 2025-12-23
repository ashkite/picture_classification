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
import com.ashkite.pictureclassification.data.model.CityEntity
import com.ashkite.pictureclassification.data.model.MediaItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PlaceDetailScreen(cityId: Long, onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val locale = LocalContext.current.resources.configuration.locales[0]
    var city by remember { mutableStateOf<CityEntity?>(null) }
    var items by remember { mutableStateOf<List<MediaItemEntity>>(emptyList()) }

    LaunchedEffect(cityId) {
        withContext(Dispatchers.IO) {
            val database = AppDatabase.get(context)
            city = database.cityDao().findById(cityId)
            items = database.mediaDao().getMediaByCity(cityId, MEDIA_LIMIT)
        }
    }

    MediaGridScreen(
        title = city?.let { formatCityName(it, locale) }
            ?: stringResource(R.string.detail_place_unknown),
        subtitle = city?.countryCode?.ifBlank { null },
        items = items,
        onBack = onBack
    )
}

private const val MEDIA_LIMIT = 500

private fun formatCityName(city: CityEntity, locale: java.util.Locale): String {
    val prefersKorean = locale.language == "ko"
    return if (prefersKorean) {
        city.nameKo.ifBlank { city.nameEn }
    } else {
        city.nameEn.ifBlank { city.nameKo }
    }
}

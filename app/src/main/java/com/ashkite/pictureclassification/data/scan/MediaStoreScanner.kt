package com.ashkite.pictureclassification.data.scan

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.ashkite.pictureclassification.data.geo.CityGeocoder
import com.ashkite.pictureclassification.data.model.MediaItemEntity
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreScanner(
    private val context: Context,
    private val metadataReader: MediaMetadataReader,
    private val geocoder: CityGeocoder
) {
    suspend fun scan(): List<MediaItemEntity> = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val volume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.VOLUME_EXTERNAL
        } else {
            "external"
        }
        val collection = MediaStore.Files.getContentUri(volume)
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_TAKEN,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        val selection = (
            "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR " +
                "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"
        )
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_TAKEN} DESC"
        val now = System.currentTimeMillis()
        val zoneId = ZoneId.systemDefault()

        val results = mutableListOf<MediaItemEntity>()
        resolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val takenIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val modifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val mediaType = cursor.getInt(typeIndex)
                val mimeType = cursor.getString(mimeIndex)
                val dateTaken = cursor.getLong(takenIndex)
                val dateModified = cursor.getLong(modifiedIndex) * 1000
                val effectiveDate = if (dateTaken > 0L) dateTaken else dateModified
                val defaultInstant = Instant.ofEpochMilli(effectiveDate)

                val contentUri = when (mediaType) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ->
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ->
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    else -> ContentUris.withAppendedId(collection, id)
                }

                val isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                val metadata = metadataReader.read(contentUri, isVideo)
                val captureInstant = metadata.captureInstant ?: defaultInstant
                val offsetMinutes = metadata.tzOffsetMin
                    ?: zoneId.rules.getOffset(captureInstant).totalSeconds / 60
                val localDate = captureInstant
                    .atOffset(ZoneOffset.ofTotalSeconds(offsetMinutes * 60))
                    .toLocalDate()
                    .toString()
                val lat = metadata.lat
                val lon = metadata.lon
                val hasLocation = lat != null && lon != null
                val cityId = if (hasLocation) {
                    geocoder.findCity(lat!!, lon!!)?.id
                } else {
                    null
                }

                results += MediaItemEntity(
                    uri = contentUri.toString(),
                    mediaStoreId = id,
                    mimeType = mimeType,
                    isVideo = isVideo,
                    dateTakenUtc = captureInstant.toEpochMilli(),
                    tzOffsetMin = offsetMinutes,
                    localDate = localDate,
                    lat = lat,
                    lon = lon,
                    cityId = cityId,
                    hasLocation = hasLocation,
                    phash = null,
                    faceCount = null,
                    labelJson = null,
                    scanVersion = 1,
                    lastScannedAt = now
                )
            }
        }

        results
    }
}

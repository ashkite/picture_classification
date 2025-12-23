package com.ashkite.pictureclassification.data.scan

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.ashkite.pictureclassification.data.model.MediaItemEntity
import java.time.Instant
import java.time.ZoneId
import android.os.Build

class MediaStoreScanner(private val context: Context) {
    fun scan(): List<MediaItemEntity> {
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
                val instant = Instant.ofEpochMilli(effectiveDate)
                val offset = zoneId.rules.getOffset(instant)
                val tzOffsetMin = offset.totalSeconds / 60
                val localDate = instant.atZone(zoneId).toLocalDate().toString()

                val contentUri = when (mediaType) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ->
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ->
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    else -> ContentUris.withAppendedId(collection, id)
                }

                results += MediaItemEntity(
                    uri = contentUri.toString(),
                    mediaStoreId = id,
                    mimeType = mimeType,
                    isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                    dateTakenUtc = effectiveDate,
                    tzOffsetMin = tzOffsetMin,
                    localDate = localDate,
                    lat = null,
                    lon = null,
                    cityId = null,
                    hasLocation = false,
                    phash = null,
                    faceCount = null,
                    labelJson = null,
                    scanVersion = 1,
                    lastScannedAt = now
                )
            }
        }

        return results
    }
}

package com.ashkite.pictureclassification.data.scan

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class MediaMetadataReader(private val context: Context) {
    fun read(uri: Uri, isVideo: Boolean): MediaMetadata {
        return if (isVideo) {
            readVideo(uri)
        } else {
            readImage(uri)
        }
    }

    private fun readImage(uri: Uri): MediaMetadata {
        val resolver = context.contentResolver
        return try {
            resolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val latLong = FloatArray(2)
                val hasLatLong = exif.getLatLong(latLong)
                val lat = if (hasLatLong) latLong[0].toDouble() else null
                val lon = if (hasLatLong) latLong[1].toDouble() else null
                val offset = exif.getAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_OFFSET_TIME)
                val tzOffsetMin = parseOffsetMinutes(offset)
                val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                val captureInstant = parseExifDateTime(dateTime, tzOffsetMin)
                MediaMetadata(captureInstant, tzOffsetMin, lat, lon)
            } ?: MediaMetadata(null, null, null, null)
        } catch (ex: Exception) {
            MediaMetadata(null, null, null, null)
        }
    }

    private fun readVideo(uri: Uri): MediaMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val location = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION)
            val (lat, lon) = parseIso6709(location)
            val date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
            val instant = parseVideoDate(date)
            val tzOffsetMin = if (instant != null) 0 else null
            MediaMetadata(instant, tzOffsetMin, lat, lon)
        } catch (ex: Exception) {
            MediaMetadata(null, null, null, null)
        } finally {
            retriever.release()
        }
    }

    private fun parseExifDateTime(value: String?, offsetMin: Int?): Instant? {
        if (value.isNullOrBlank()) return null
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")
            val localDateTime = LocalDateTime.parse(value, formatter)
            if (offsetMin != null) {
                val offset = ZoneOffset.ofTotalSeconds(offsetMin * 60)
                localDateTime.toInstant(offset)
            } else {
                localDateTime.atZone(ZoneId.systemDefault()).toInstant()
            }
        } catch (ex: Exception) {
            null
        }
    }

    private fun parseOffsetMinutes(value: String?): Int? {
        if (value.isNullOrBlank()) return null
        val match = OFFSET_REGEX.matchEntire(value.trim()) ?: return null
        val sign = if (match.groupValues[1] == "-") -1 else 1
        val hours = match.groupValues[2].toIntOrNull() ?: return null
        val minutes = match.groupValues[3].toIntOrNull() ?: return null
        return sign * (hours * 60 + minutes)
    }

    private fun parseIso6709(value: String?): Pair<Double?, Double?> {
        if (value.isNullOrBlank()) return null to null
        val cleaned = value.trim().removeSuffix("/")
        val match = ISO6709_REGEX.matchEntire(cleaned) ?: return null to null
        val lat = match.groupValues[1].toDoubleOrNull()
        val lon = match.groupValues[2].toDoubleOrNull()
        return lat to lon
    }

    private fun parseVideoDate(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        val patterns = listOf(
            "yyyyMMdd'T'HHmmss.SSS'Z'",
            "yyyyMMdd'T'HHmmss'Z'"
        )
        for (pattern in patterns) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneOffset.UTC)
                return Instant.from(formatter.parse(value))
            } catch (ex: Exception) {
                // Try next pattern.
            }
        }
        return null
    }

    companion object {
        private val OFFSET_REGEX = Regex("([+-])(\\d{2}):(\\d{2})")
        private val ISO6709_REGEX = Regex("([+-]\\d+(?:\\.\\d+)?)([+-]\\d+(?:\\.\\d+)?)")
    }
}

data class MediaMetadata(
    val captureInstant: Instant?,
    val tzOffsetMin: Int?,
    val lat: Double?,
    val lon: Double?
)

package com.ashkite.pictureclassification.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media_item",
    indices = [
        Index(value = ["localDate"]),
        Index(value = ["cityId"]),
        Index(value = ["hasLocation"]),
        Index(value = ["mediaStoreId"])
    ]
)
data class MediaItemEntity(
    @PrimaryKey val uri: String,
    val mediaStoreId: Long,
    val mimeType: String?,
    val isVideo: Boolean,
    val dateTakenUtc: Long,
    val tzOffsetMin: Int,
    val localDate: String,
    val lat: Double?,
    val lon: Double?,
    val cityId: Long?,
    val hasLocation: Boolean,
    val phash: String?,
    val faceCount: Int?,
    val labelJson: String?,
    val scanVersion: Int,
    val lastScannedAt: Long
)

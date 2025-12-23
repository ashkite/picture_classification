package com.ashkite.pictureclassification.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "face_cluster")
data class FaceClusterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val centroid: ByteArray,
    val userLabel: String?,
    val sampleMediaUri: String?
)

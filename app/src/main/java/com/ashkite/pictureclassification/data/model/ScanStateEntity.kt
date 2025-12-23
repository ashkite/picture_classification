package com.ashkite.pictureclassification.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_state")
data class ScanStateEntity(
    @PrimaryKey val id: Int = 0,
    val lastScanEpoch: Long,
    val lastSuccessEpoch: Long,
    val schemaVersion: Int,
    val errorCount: Int
)

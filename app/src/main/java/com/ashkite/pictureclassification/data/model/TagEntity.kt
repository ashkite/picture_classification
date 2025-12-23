package com.ashkite.pictureclassification.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tag",
    indices = [Index(value = ["type", "name"], unique = true)]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val name: String,
    val confidence: Double,
    val source: String
)

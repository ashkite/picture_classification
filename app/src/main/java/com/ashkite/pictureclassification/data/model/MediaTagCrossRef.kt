package com.ashkite.pictureclassification.data.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "media_tag",
    primaryKeys = ["mediaUri", "tagId"],
    indices = [Index(value = ["tagId"])]
)
data class MediaTagCrossRef(
    val mediaUri: String,
    val tagId: Long
)

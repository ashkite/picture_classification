package com.ashkite.pictureclassification.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "city",
    indices = [
        Index(value = ["geohash"]),
        Index(value = ["countryCode"])
    ]
)
data class CityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameKo: String,
    val nameEn: String,
    val countryCode: String,
    val lat: Double,
    val lon: Double,
    val geohash: String
)

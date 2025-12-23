package com.ashkite.pictureclassification.data.model

data class DateCount(
    val localDate: String,
    val count: Int
)

data class PlaceCount(
    val cityId: Long,
    val nameKo: String,
    val nameEn: String,
    val countryCode: String,
    val count: Int
)

data class TagCount(
    val tagId: Long,
    val name: String,
    val type: String,
    val count: Int
)

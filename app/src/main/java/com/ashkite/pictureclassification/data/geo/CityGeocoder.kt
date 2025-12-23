package com.ashkite.pictureclassification.data.geo

import com.ashkite.pictureclassification.data.db.CityDao
import com.ashkite.pictureclassification.data.model.CityEntity

class CityGeocoder(private val cityDao: CityDao) {
    suspend fun findCity(lat: Double, lon: Double): CityEntity? {
        val geohash = GeoHash.encode(lat, lon, precision = 6)
        val prefixes = listOf(
            geohash,
            geohash.dropLast(1),
            geohash.dropLast(2)
        )
        var best: CityEntity? = null
        var bestDistance = Double.MAX_VALUE

        for (prefix in prefixes) {
            if (prefix.isEmpty()) continue
            val candidates = cityDao.findByGeohashPrefix(prefix, 200)
            for (candidate in candidates) {
                val distance = GeoUtils.distanceKm(lat, lon, candidate.lat, candidate.lon)
                if (distance < bestDistance) {
                    bestDistance = distance
                    best = candidate
                }
            }
            if (bestDistance <= MAX_DISTANCE_KM) {
                return best
            }
        }

        return if (bestDistance <= MAX_DISTANCE_KM) best else null
    }

    companion object {
        private const val MAX_DISTANCE_KM = 50.0
    }
}

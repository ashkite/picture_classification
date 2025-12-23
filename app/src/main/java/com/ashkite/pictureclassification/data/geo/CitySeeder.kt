package com.ashkite.pictureclassification.data.geo

import android.content.Context
import com.ashkite.pictureclassification.data.db.CityDao
import com.ashkite.pictureclassification.data.model.CityEntity
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CitySeeder(
    private val context: Context,
    private val cityDao: CityDao
) {
    suspend fun seedIfNeeded(): Int = withContext(Dispatchers.IO) {
        if (cityDao.count() > 0) return@withContext 0
        val items = mutableListOf<CityEntity>()
        val assetName = ASSET_FILE

        val stream = try {
            context.assets.open(assetName)
        } catch (ex: Exception) {
            return@withContext 0
        }

        stream.use { input ->
            BufferedReader(InputStreamReader(input)).use { reader ->
                var lineIndex = 0
                while (true) {
                    val line = reader.readLine() ?: break
                    if (lineIndex == 0 && line.startsWith("name_en")) {
                        lineIndex++
                        continue
                    }
                    val row = parseLine(line)
                    if (row != null) {
                        val geohash = GeoHash.encode(row.lat, row.lon, precision = 6)
                        items += CityEntity(
                            nameKo = row.nameKo,
                            nameEn = row.nameEn,
                            countryCode = row.countryCode,
                            lat = row.lat,
                            lon = row.lon,
                            geohash = geohash
                        )
                        if (items.size >= BATCH_SIZE) {
                            cityDao.upsertAll(items.toList())
                            items.clear()
                        }
                    }
                    lineIndex++
                }
            }
        }

        if (items.isNotEmpty()) {
            cityDao.upsertAll(items.toList())
        }
        return@withContext cityDao.count()
    }

    private fun parseLine(line: String): CitySeedRow? {
        if (line.isBlank()) return null
        val parts = line.split(',', limit = 5).map { it.trim() }
        if (parts.size < 5) return null
        val lat = parts[3].toDoubleOrNull() ?: return null
        val lon = parts[4].toDoubleOrNull() ?: return null
        return CitySeedRow(
            nameEn = parts[0],
            nameKo = parts[1].ifBlank { parts[0] },
            countryCode = parts[2],
            lat = lat,
            lon = lon
        )
    }

    private data class CitySeedRow(
        val nameEn: String,
        val nameKo: String,
        val countryCode: String,
        val lat: Double,
        val lon: Double
    )

    companion object {
        private const val ASSET_FILE = "cities_seed.csv"
        private const val BATCH_SIZE = 500
    }
}

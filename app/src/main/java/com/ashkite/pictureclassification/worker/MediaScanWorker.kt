package com.ashkite.pictureclassification.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.ashkite.pictureclassification.data.db.AppDatabase
import com.ashkite.pictureclassification.data.geo.CityGeocoder
import com.ashkite.pictureclassification.data.geo.CitySeeder
import com.ashkite.pictureclassification.data.repo.AutoTagger
import com.ashkite.pictureclassification.data.repo.MediaRepository
import com.ashkite.pictureclassification.data.scan.MediaMetadataReader
import com.ashkite.pictureclassification.data.scan.MediaStoreScanner

class MediaScanWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.get(applicationContext)
        if (database.cityDao().count() == 0) {
            CitySeeder(applicationContext, database.cityDao()).seedIfNeeded()
        }
        val geocoder = CityGeocoder(database.cityDao())
        val metadataReader = MediaMetadataReader(applicationContext)
        val scanner = MediaStoreScanner(applicationContext, metadataReader, geocoder)
        val repository = MediaRepository(database, scanner)

        return try {
            val count = repository.scanAndStore()
            try {
                AutoTagger(applicationContext, database).use { tagger ->
                    tagger.tagBatch(AUTO_TAG_LIMIT)
                }
            } catch (_: Exception) {
                // Auto-tagging failures should not block scanning results.
            }
            Result.success(
                Data.Builder()
                    .putInt(KEY_MEDIA_COUNT, count)
                    .build()
            )
        } catch (ex: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_MEDIA_COUNT = "media_count"
        private const val AUTO_TAG_LIMIT = 200
    }
}

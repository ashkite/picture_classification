package com.ashkite.pictureclassification.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.ashkite.pictureclassification.data.db.AppDatabase
import com.ashkite.pictureclassification.data.repo.MediaRepository
import com.ashkite.pictureclassification.data.scan.MediaStoreScanner

class MediaScanWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.get(applicationContext)
        val scanner = MediaStoreScanner(applicationContext)
        val repository = MediaRepository(database, scanner)

        return try {
            val count = repository.scanAndStore()
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
    }
}

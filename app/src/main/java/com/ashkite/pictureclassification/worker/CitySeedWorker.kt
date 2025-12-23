package com.ashkite.pictureclassification.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.ashkite.pictureclassification.data.db.AppDatabase
import com.ashkite.pictureclassification.data.geo.CitySeeder

class CitySeedWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.get(applicationContext)
        val seeder = CitySeeder(applicationContext, database.cityDao())
        val count = seeder.seedIfNeeded()
        return Result.success(
            Data.Builder()
                .putInt(KEY_CITY_COUNT, count)
                .build()
        )
    }

    companion object {
        const val KEY_CITY_COUNT = "city_count"
    }
}

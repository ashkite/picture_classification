package com.ashkite.pictureclassification.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object MediaScanScheduler {
    private const val UNIQUE_ONE_TIME = "media_scan_one_time"
    private const val UNIQUE_PERIODIC = "media_scan_periodic"

    fun enqueueOneTime(context: Context, force: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()
        val request = OneTimeWorkRequestBuilder<MediaScanWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_ONE_TIME,
            if (force) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun enqueuePeriodic(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()
        val request = PeriodicWorkRequestBuilder<MediaScanWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

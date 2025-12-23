package com.ashkite.pictureclassification.data.repo

import com.ashkite.pictureclassification.data.db.AppDatabase
import com.ashkite.pictureclassification.data.model.ScanStateEntity
import com.ashkite.pictureclassification.data.scan.MediaStoreScanner

class MediaRepository(
    private val database: AppDatabase,
    private val scanner: MediaStoreScanner
) {
    suspend fun scanAndStore(): Int {
        val now = System.currentTimeMillis()
        return try {
            val items = scanner.scan()
            if (items.isNotEmpty()) {
                database.mediaDao().upsertAll(items)
            }
            val state = ScanStateEntity(
                id = 0,
                lastScanEpoch = now,
                lastSuccessEpoch = now,
                schemaVersion = 1,
                errorCount = 0
            )
            database.scanStateDao().upsert(state)
            items.size
        } catch (ex: Exception) {
            val previous = database.scanStateDao().get()
            val state = ScanStateEntity(
                id = 0,
                lastScanEpoch = now,
                lastSuccessEpoch = previous?.lastSuccessEpoch ?: 0L,
                schemaVersion = 1,
                errorCount = (previous?.errorCount ?: 0) + 1
            )
            database.scanStateDao().upsert(state)
            throw ex
        }
    }
}

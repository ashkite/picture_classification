package com.ashkite.pictureclassification.data.repo

import com.ashkite.pictureclassification.data.db.AppDatabase
import com.ashkite.pictureclassification.data.model.MediaTagCrossRef
import com.ashkite.pictureclassification.data.model.TagEntity

class TagRepository(private val database: AppDatabase) {
    suspend fun addManualTagToMedia(mediaUri: String, type: String, name: String) {
        addTagToMedia(mediaUri, type, name, SOURCE_MANUAL, 1.0)
    }

    suspend fun addAutoTagToMedia(mediaUri: String, type: String, name: String, confidence: Double) {
        addTagToMedia(mediaUri, type, name, SOURCE_MODEL, confidence)
    }

    private suspend fun addTagToMedia(
        mediaUri: String,
        type: String,
        name: String,
        source: String,
        confidence: Double
    ) {
        val cleanedName = name.trim()
        if (cleanedName.isEmpty()) return
        val tagDao = database.tagDao()
        val mediaTagDao = database.mediaTagDao()
        val insertedId = tagDao.insert(
            TagEntity(
                type = type,
                name = cleanedName,
                confidence = confidence,
                source = source
            )
        )
        val tag = if (insertedId != -1L) {
            tagDao.findById(insertedId)
        } else {
            tagDao.findByTypeAndName(type, cleanedName)
        }
        if (tag != null) {
            mediaTagDao.insertAll(listOf(MediaTagCrossRef(mediaUri = mediaUri, tagId = tag.id)))
        }
    }

    companion object {
        private const val SOURCE_MANUAL = "manual"
        private const val SOURCE_MODEL = "model"
    }
}

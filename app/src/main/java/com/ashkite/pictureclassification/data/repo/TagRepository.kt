package com.ashkite.pictureclassification.data.repo

import com.ashkite.pictureclassification.data.db.AppDatabase
import com.ashkite.pictureclassification.data.model.MediaTagCrossRef
import com.ashkite.pictureclassification.data.model.TagEntity

class TagRepository(private val database: AppDatabase) {
    suspend fun addTagToMedia(mediaUri: String, type: String, name: String) {
        val cleanedName = name.trim()
        if (cleanedName.isEmpty()) return
        val tagDao = database.tagDao()
        val mediaTagDao = database.mediaTagDao()
        val insertedId = tagDao.insert(
            TagEntity(
                type = type,
                name = cleanedName,
                confidence = 1.0,
                source = SOURCE_MANUAL
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
    }
}

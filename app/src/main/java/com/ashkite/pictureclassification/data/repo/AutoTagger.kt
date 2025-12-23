package com.ashkite.pictureclassification.data.repo

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import android.graphics.ImageDecoder
import com.ashkite.pictureclassification.data.db.AppDatabase
import com.ashkite.pictureclassification.data.ml.EventLabelMapper
import com.ashkite.pictureclassification.data.ml.TfliteImageClassifier
import com.ashkite.pictureclassification.data.model.MediaItemEntity
import java.io.Closeable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AutoTagger(
    private val context: Context,
    private val database: AppDatabase
) : Closeable {
    private val classifier = TfliteImageClassifier(context)
    private val mapper = EventLabelMapper()
    private val tagRepository = TagRepository(database)

    suspend fun tagBatch(limit: Int): Int = withContext(Dispatchers.IO) {
        val mediaDao = database.mediaDao()
        val items = mediaDao.getUnlabeledMedia(limit)
        items.forEach { item ->
            val bitmap = loadBitmap(item)
            if (bitmap == null) {
                mediaDao.updateLabel(item.uri, LABEL_EMPTY)
                return@forEach
            }
            val labels = classifier.classify(bitmap, TOP_K)
            val tags = mapper.mapEvents(labels) + mapper.mapPeople(labels)
            tags.forEach { tag ->
                tagRepository.addAutoTagToMedia(
                    mediaUri = item.uri,
                    type = tag.type,
                    name = tag.name,
                    confidence = tag.score.toDouble()
                )
            }
            mediaDao.updateLabel(item.uri, buildLabelJson(tags))
        }
        items.size
    }

    override fun close() {
        classifier.close()
    }

    private fun loadBitmap(item: MediaItemEntity): Bitmap? {
        return if (item.isVideo) {
            loadVideoFrame(item)
        } else {
            loadImage(item)
        }
    }

    private fun loadImage(item: MediaItemEntity): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                loadImageDecoder(item)
            } else {
                null
            }
        } catch (ex: Exception) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun loadImageDecoder(item: MediaItemEntity): Bitmap? {
        val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(item.uri))
        return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.setTargetSize(classifier.inputWidth, classifier.inputHeight)
            decoder.isMutableRequired = false
        }
    }

    private fun loadVideoFrame(item: MediaItemEntity): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(item.uri))
            retriever.getFrameAtTime(0)
        } catch (ex: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun buildLabelJson(tags: List<com.ashkite.pictureclassification.data.ml.AutoTag>): String {
        if (tags.isEmpty()) return LABEL_EMPTY
        val grouped = tags.groupBy { it.type }
        val root = JSONObject()
        grouped.forEach { (type, items) ->
            val array = JSONArray()
            items.forEach { tag ->
                val obj = JSONObject()
                obj.put("name", tag.name)
                obj.put("score", tag.score)
                array.put(obj)
            }
            root.put(type, array)
        }
        return root.toString()
    }

    companion object {
        private const val TOP_K = 5
        private const val LABEL_EMPTY = "{}"
    }
}

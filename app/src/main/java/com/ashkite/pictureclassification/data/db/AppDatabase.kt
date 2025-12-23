package com.ashkite.pictureclassification.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ashkite.pictureclassification.data.model.CityEntity
import com.ashkite.pictureclassification.data.model.FaceClusterEntity
import com.ashkite.pictureclassification.data.model.MediaItemEntity
import com.ashkite.pictureclassification.data.model.MediaTagCrossRef
import com.ashkite.pictureclassification.data.model.ScanStateEntity
import com.ashkite.pictureclassification.data.model.TagEntity

@Database(
    entities = [
        MediaItemEntity::class,
        CityEntity::class,
        TagEntity::class,
        MediaTagCrossRef::class,
        FaceClusterEntity::class,
        ScanStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun cityDao(): CityDao
    abstract fun tagDao(): TagDao
    abstract fun mediaTagDao(): MediaTagDao
    abstract fun faceClusterDao(): FaceClusterDao
    abstract fun scanStateDao(): ScanStateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "picture_classification.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

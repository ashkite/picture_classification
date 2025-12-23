package com.ashkite.pictureclassification.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.ashkite.pictureclassification.data.model.CityEntity
import com.ashkite.pictureclassification.data.model.FaceClusterEntity
import com.ashkite.pictureclassification.data.model.MediaItemEntity
import com.ashkite.pictureclassification.data.model.MediaTagCrossRef
import com.ashkite.pictureclassification.data.model.ScanStateEntity
import com.ashkite.pictureclassification.data.model.TagEntity

@Dao
interface MediaDao {
    @Upsert
    suspend fun upsertAll(items: List<MediaItemEntity>)

    @Query("SELECT * FROM media_item WHERE uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): MediaItemEntity?

    @Query("SELECT COUNT(*) FROM media_item")
    suspend fun count(): Int
}

@Dao
interface CityDao {
    @Upsert
    suspend fun upsertAll(items: List<CityEntity>)

    @Query("SELECT * FROM city WHERE geohash = :geohash")
    suspend fun findByGeohash(geohash: String): List<CityEntity>

    @Query("SELECT * FROM city WHERE geohash LIKE :prefix || '%' LIMIT :limit")
    suspend fun findByGeohashPrefix(prefix: String, limit: Int): List<CityEntity>

    @Query("SELECT COUNT(*) FROM city")
    suspend fun count(): Int
}

@Dao
interface TagDao {
    @Upsert
    suspend fun upsert(tag: TagEntity)
}

@Dao
interface MediaTagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<MediaTagCrossRef>)
}

@Dao
interface FaceClusterDao {
    @Upsert
    suspend fun upsert(cluster: FaceClusterEntity)
}

@Dao
interface ScanStateDao {
    @Query("SELECT * FROM scan_state WHERE id = 0")
    suspend fun get(): ScanStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: ScanStateEntity)
}

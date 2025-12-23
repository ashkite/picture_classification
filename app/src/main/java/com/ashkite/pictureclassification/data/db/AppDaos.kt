package com.ashkite.pictureclassification.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.ashkite.pictureclassification.data.model.CityEntity
import com.ashkite.pictureclassification.data.model.DateCount
import com.ashkite.pictureclassification.data.model.FaceClusterEntity
import com.ashkite.pictureclassification.data.model.MediaItemEntity
import com.ashkite.pictureclassification.data.model.MediaTagCrossRef
import com.ashkite.pictureclassification.data.model.PlaceCount
import com.ashkite.pictureclassification.data.model.ScanStateEntity
import com.ashkite.pictureclassification.data.model.TagCount
import com.ashkite.pictureclassification.data.model.TagEntity

@Dao
interface MediaDao {
    @Upsert
    suspend fun upsertAll(items: List<MediaItemEntity>)

    @Query("SELECT * FROM media_item WHERE uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): MediaItemEntity?

    @Query("SELECT COUNT(*) FROM media_item")
    suspend fun count(): Int

    @Query(
        "SELECT localDate AS localDate, COUNT(*) AS count " +
            "FROM media_item GROUP BY localDate ORDER BY localDate DESC LIMIT :limit"
    )
    suspend fun getDateCounts(limit: Int): List<DateCount>

    @Query(
        "SELECT city.id AS cityId, city.nameKo AS nameKo, city.nameEn AS nameEn, " +
            "city.countryCode AS countryCode, COUNT(media_item.uri) AS count " +
            "FROM media_item INNER JOIN city ON media_item.cityId = city.id " +
            "GROUP BY city.id ORDER BY count DESC LIMIT :limit"
    )
    suspend fun getPlaceCounts(limit: Int): List<PlaceCount>

    @Query("SELECT COUNT(*) FROM media_item WHERE hasLocation = 0")
    suspend fun countLocationUnknown(): Int

    @Query(
        "SELECT localDate AS localDate, COUNT(*) AS count " +
            "FROM media_item WHERE hasLocation = 0 " +
            "GROUP BY localDate ORDER BY localDate DESC LIMIT :limit"
    )
    suspend fun getUnknownDateCounts(limit: Int): List<DateCount>
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

    @Query(
        "SELECT tag.id AS tagId, tag.name AS name, tag.type AS type, " +
            "COUNT(media_tag.mediaUri) AS count " +
            "FROM tag INNER JOIN media_tag ON tag.id = media_tag.tagId " +
            "WHERE tag.type = :type GROUP BY tag.id ORDER BY count DESC LIMIT :limit"
    )
    suspend fun getTagCounts(type: String, limit: Int): List<TagCount>
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

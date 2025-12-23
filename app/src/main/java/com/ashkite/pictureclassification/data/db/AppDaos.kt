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

    @Query(
        "SELECT * FROM media_item WHERE cityId = :cityId " +
            "ORDER BY dateTakenUtc DESC LIMIT :limit"
    )
    suspend fun getMediaByCity(cityId: Long, limit: Int): List<MediaItemEntity>

    @Query(
        "SELECT * FROM media_item WHERE localDate = :localDate " +
            "ORDER BY dateTakenUtc DESC LIMIT :limit"
    )
    suspend fun getMediaByDate(localDate: String, limit: Int): List<MediaItemEntity>

    @Query(
        "SELECT * FROM media_item WHERE hasLocation = 0 " +
            "ORDER BY dateTakenUtc DESC LIMIT :limit"
    )
    suspend fun getUnknownMedia(limit: Int): List<MediaItemEntity>

    @Query(
        "SELECT * FROM media_item WHERE hasLocation = 0 AND localDate = :localDate " +
            "ORDER BY dateTakenUtc DESC LIMIT :limit"
    )
    suspend fun getUnknownMediaByDate(localDate: String, limit: Int): List<MediaItemEntity>

    @Query(
        "SELECT media_item.* FROM media_item " +
            "INNER JOIN media_tag ON media_item.uri = media_tag.mediaUri " +
            "WHERE media_tag.tagId = :tagId " +
            "ORDER BY media_item.dateTakenUtc DESC LIMIT :limit"
    )
    suspend fun getMediaByTag(tagId: Long, limit: Int): List<MediaItemEntity>
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

    @Query("SELECT * FROM city WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): CityEntity?
}

@Dao
interface TagDao {
    @Upsert
    suspend fun upsert(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity): Long

    @Query("SELECT * FROM tag WHERE type = :type AND name = :name LIMIT 1")
    suspend fun findByTypeAndName(type: String, name: String): TagEntity?

    @Query("SELECT * FROM tag WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): TagEntity?

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

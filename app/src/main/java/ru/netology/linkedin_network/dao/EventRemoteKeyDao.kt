package ru.netology.linkedin_network.dao

import androidx.room.*
import ru.netology.linkedin_network.entity.EventRemoteKeyEntity

@Dao
interface EventRemoteKeyDao {
    @Query("SELECT COUNT(*) == 0 FROM EventRemoteKeyEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT MAX(id) FROM EventRemoteKeyEntity")
    suspend fun max(): Int?

    @Query("SELECT MIN(id) FROM EventRemoteKeyEntity")
    suspend fun min(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: EventRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<EventRemoteKeyEntity>)

    @Query("DELETE FROM EventRemoteKeyEntity")
    suspend fun removeAllEvents()
}
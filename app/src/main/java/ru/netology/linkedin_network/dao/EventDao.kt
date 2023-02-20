package ru.netology.linkedin_network.dao

import androidx.paging.PagingSource
import androidx.room.*
import ru.netology.linkedin_network.entity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY id DESC")
    fun getAllEvents(): PagingSource<Int, EventEntity>

    @Query("SELECT COUNT(*) == 0 FROM events")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<EventEntity>)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun removeById(id: Int)

    @Query("SELECT * FROM events ORDER BY id DESC LIMIT 1")
    suspend fun getEventEventMaxId(): EventEntity?

    @Query("SELECT COUNT() FROM events")
    suspend fun count(): Int

    @Query("DELETE FROM events")
    suspend fun removeAllEvents()

}
package ru.netology.linkedin_network.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.linkedin_network.entity.JobEntity

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY id DESC")
    fun getAllJobs(): LiveData<List<JobEntity>>

    @Query("SELECT COUNT(*) == 0 FROM jobs")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: List<JobEntity>)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun removeJobById(id: Int)

    @Query("SELECT COUNT() FROM jobs")
    suspend fun count(): Int

    @Query("DELETE FROM jobs")
    suspend fun removeAllJobs()
}
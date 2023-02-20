package ru.netology.linkedin_network.dao

import androidx.paging.PagingSource
import androidx.room.*
import ru.netology.linkedin_network.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAllPosts(): PagingSource<Int, PostEntity>

    @Query("SELECT COUNT(*) == 0 FROM posts")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun removeById(id: Int)

    @Query("SELECT COUNT() FROM posts")
    suspend fun count(): Int

    @Query("DELETE FROM posts")
    suspend fun removeAllPosts()
}
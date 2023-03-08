package ru.netology.linkedin_network.repository.post

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import ru.netology.linkedin_network.dto.*
import ru.netology.linkedin_network.enumeration.AttachmentType
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface PostRepository {
    val data: Flow<PagingData<Post>>
    val postUsersData: MutableLiveData<List<UserPreview>>
    val postMentionsData: MutableLiveData<List<UserPreview>>
    val postLikersData: MutableLiveData<List<UserPreview>>
    suspend fun getAll()
    fun loadUserWall(userId: Int): Flow<PagingData<Post>>
    suspend fun getLikedAndMentionedUsersList(post: Post)
    suspend fun getMentions(post: Post)
    suspend fun getLikers(post: Post)
    suspend fun removePostById(id: Int)
    suspend fun likePost(post: Post): Post
     suspend fun getPostById(id: Int): Post
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: Int): User
    suspend fun addMediaToPost(type: AttachmentType, file: MultipartBody.Part): Attachment
    suspend fun savePost(post: Post)

}
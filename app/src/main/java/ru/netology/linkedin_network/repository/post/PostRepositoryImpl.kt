package ru.netology.linkedin_network.repository.post

import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import ru.netology.linkedin_network.api.ApiService
import ru.netology.linkedin_network.dao.PostDao
import ru.netology.linkedin_network.dto.*
import ru.netology.linkedin_network.entity.PostEntity
import ru.netology.linkedin_network.enumeration.AttachmentType
import ru.netology.linkedin_network.error.ApiError
import ru.netology.linkedin_network.error.NetworkError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import ru.netology.linkedin_network.error.UnknownError
import ru.netology.linkedin_network.repository.mediator.PostRemoteMediator
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    mediator: PostRemoteMediator,
    private val dao: PostDao
) : PostRepository {

    override var data: Flow<PagingData<Post>> =
        Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { dao.getAllPosts() },
            remoteMediator = mediator
        ).flow.map {
            it.map(PostEntity::toDto)
        }


    override val postUsersData: MutableLiveData<List<UserPreview>> = MutableLiveData(emptyList())
    override val postLikersData: MutableLiveData<List<UserPreview>> = MutableLiveData(emptyList())
    override val postMentionsData: MutableLiveData<List<UserPreview>> = MutableLiveData(emptyList())
    override suspend fun getAll() {
        try {
            val response = apiService.getAllPost()
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            val posts = response.body() ?: throw RuntimeException("body is null")

            dao.insert(posts.map(PostEntity.Companion::fromDto))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
    override suspend fun getLikedAndMentionedUsersList(post: Post) {
        try {
            val response = apiService.getPostById(post.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val usersList = response.body()?.users!!
            for ((key, value) in usersList) {
                if (post.likeOwnerIds.contains(key)) value.isLiked = true
                if (post.mentionIds.contains(key)) value.isMentioned = true
            }
            val list = usersList.values.toMutableList()
            postUsersData.postValue(list)
        } catch (e: IOException) {
            throw NetworkError
        }
    }
    override suspend fun getLikers(post: Post) {
        try {
            postLikersData.postValue(emptyList())
            val response = apiService.getPostById(post.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val usersList = response.body()?.users!!
            val menions = mutableListOf<UserPreview>()
            for ((key, value) in usersList) {
                if (post.likeOwnerIds.contains(key)) {
                    value.isLiked = true
                    menions.add(value)
                }

            }
            postLikersData.postValue(menions)
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getMentions(post: Post) {
        try {
            postMentionsData.postValue(emptyList())
            val response = apiService.getPostById(post.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val usersList = response.body()?.users!!
            val liked = mutableListOf<UserPreview>()
            for ((key, value) in usersList) {
                if (post.mentionIds.contains(key)) {
                    value.isMentioned = true
                    liked.add(value)
                }
            }
            postMentionsData.postValue(liked)
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun removePostById(id: Int) {
        try {
            val response = apiService.removePostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            dao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun likePost(post: Post): Post {
            val newPost: Post?
            if (post.likedByMe) {
                return disLike(post)
        }
        try {
            val response = apiService.likePost(post.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            newPost = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(newPost))
            return newPost
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    private suspend fun disLike(post: Post): Post {
        val newPost:Post?
        try {
            val response = apiService.dislikePost(post.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            newPost = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(newPost))
            return newPost
        } catch (e: IOException) {
            throw NetworkError
        }
    }



    override suspend fun addMediaToPost(
        type: AttachmentType,
        file: MultipartBody.Part
    ): Attachment {
        try {
            val response = apiService.uploadMedia(file)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val Media =
                response.body() ?: throw ApiError(response.code(), response.message())
            return Attachment(Media.url, type)
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getUsers(): List<User> {
        try {
            val response = apiService.getAllUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun savePost(post: Post) {
        try {
            val response = apiService.savePost(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            } else {
                val body =
                    response.body() ?: throw ApiError(response.code(), response.message())
                dao.insert(PostEntity.fromDto(body))
            }
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getPostById(id: Int): Post {
        try {
            val response = apiService.getPostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }
    override suspend fun getUserById(id: Int): User {
        try {
            val response = apiService.getUserById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            } else {
                return response.body() ?: throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        }
    }
}
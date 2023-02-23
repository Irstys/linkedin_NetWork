package ru.netology.linkedin_network.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import ru.netology.linkedin_network.api.ApiService
import ru.netology.linkedin_network.dao.EventDao
import ru.netology.linkedin_network.dto.*
import ru.netology.linkedin_network.entity.EventEntity
import ru.netology.linkedin_network.enumeration.AttachmentType
import ru.netology.linkedin_network.error.ApiError
import ru.netology.linkedin_network.error.NetworkError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class EventRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    mediator: EventRemoteMediator,
    private val dao: EventDao
) : EventRepository {
    override val data: Flow<PagingData<Event>> =
        Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { dao.getAllEvents() },
            remoteMediator = mediator
        ).flow.map {
            it.map(EventEntity::toDto)
        }
    override val eventUsersData: MutableLiveData<List<UserPreview>> = MutableLiveData(emptyList())
    override val eventSpeakersData: MutableLiveData<List<UserPreview>> = MutableLiveData(emptyList())
    override val eventParticipatesData: MutableLiveData<List<UserPreview>> = MutableLiveData(emptyList())
    override val eventLikersData: MutableLiveData<List<UserPreview>> = MutableLiveData(emptyList())

    override suspend fun getEventUsersList(event: Event) {
        try {
            val response = apiService.getEventById(event.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val usersList = response.body()?.users!!
            for ((key, value) in usersList) {
                if (event.likeOwnerIds.contains(key)) value.isLiked = true
                if (event.speakerIds.contains(key)) value.isSpeaker = true
                if (event.participantsIds.contains(key)) value.isParticipating = true
            }
            val list = usersList.values.toMutableList()
            eventUsersData.postValue(list)
        } catch (e: IOException) {
            throw NetworkError
        }
    }
    override suspend fun getSpeakers(event: Event){
        try {
            eventLikersData.postValue(emptyList())
            val response = apiService.getEventById(event.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val usersList = response.body()?.users!!
            val users = mutableListOf<UserPreview>()
            for ((key, value) in usersList) {
                if (event.speakerIds.contains(key)) {
                    value.isSpeaker = true
                    users.add(value)
                }
            }
            eventLikersData.postValue(users)
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getParticipates(event: Event){
        try {
            eventParticipatesData.postValue(emptyList())
            val response = apiService.getEventById(event.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val usersList = response.body()?.users!!
            val users = mutableListOf<UserPreview>()
            for ((key, value) in usersList) {
                if (event.participantsIds.contains(key)) {
                    value.isParticipating = true
                    users.add(value)
                }
            }
            eventParticipatesData.postValue(users)
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getLikers(event: Event) {
        try {
            eventLikersData.postValue(emptyList())
            val response = apiService.getEventById(event.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val usersList = response.body()?.users!!
            val liked = mutableListOf<UserPreview>()
            for ((key, value) in usersList) {
                if (event.likeOwnerIds.contains(key)) {
                    value.isLiked = true
                    liked.add(value)
                }
            }
            eventLikersData.postValue(liked)
        } catch (e: IOException) {
            throw NetworkError
        }
    }


    override suspend fun removeById(id: Int) {
        try {
            val response = apiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            dao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun likeEvent(event: Event): Event {
        var newEvent: Event?
        if (event.likedByMe) {
            return disLike(event)
        }
        try {
            val response = apiService.likeEvent(event.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            newEvent = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(EventEntity.fromDto(newEvent))
            return newEvent
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    private suspend fun disLike(event: Event): Event {
        var newEvent: Event?
        try {
            val response = apiService.dislikeEvent(event.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            newEvent = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(EventEntity.fromDto(newEvent))
            return newEvent
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun join(event: Event): Event {
        var newEvent: Event?
        if (event.participatedByMe) {
            return quit(event)
        }
        try {
            val response = apiService.join(event.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            newEvent= response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(EventEntity.fromDto(newEvent))
            return newEvent
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    private suspend fun quit(event: Event): Event {
        var newEvent: Event?
        try {
            val response = apiService.quitjoin(event.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            newEvent = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(EventEntity.fromDto(newEvent))
            return newEvent
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getEventById(id: Int): Event {
        try {
            val response = apiService.getEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getUsers(): List<User> {
        val usersList: List<User>
        try {
            val response = apiService.getAllUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            usersList = response.body() ?: throw ApiError(response.code(), response.message())
            return usersList
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun addMediaToEvent(
        type: AttachmentType,
        file: MultipartBody.Part
    ): Attachment {
        try {
            val response = apiService.uploadMedia(file)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val Media:Media =
                response.body() ?: throw ApiError(response.code(), response.message())
            return Attachment(Media.url, type)
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun saveEvent(event: Event) {
        try {
            val response = apiService.saveEvent(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            } else {
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                dao.insert(EventEntity.fromDto(body))
            }
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getEventRequest(id: Int): Event {
        try {
            val response = apiService.getEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            } else {
                return response.body() ?: throw ApiError(response.code(), response.message())

            }
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
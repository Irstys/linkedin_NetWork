package ru.netology.linkedin_network.repository.event

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import ru.netology.linkedin_network.dto.*
import ru.netology.linkedin_network.enumeration.AttachmentType
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface EventRepository {
    val data: Flow<PagingData<Event>>
    val eventUsersData: MutableLiveData<List<UserPreview>>
    val eventSpeakersData: MutableLiveData<List<UserPreview>>
    val eventParticipatesData: MutableLiveData<List<UserPreview>>
    val eventLikersData: MutableLiveData<List<UserPreview>>
    suspend fun getEventUsersList(event: Event)
    suspend fun getSpeakers(event: Event)
    suspend fun getParticipates(event: Event)
    suspend fun getLikers(event: Event)
    suspend fun removeById(id: Int)
    suspend fun likeEvent(event: Event): Event
    suspend fun join(event: Event): Event
    suspend fun getEventById(id: Int): Event
    suspend fun getUsers(): List<User>
    suspend fun addMediaToEvent(type: AttachmentType, file: MultipartBody.Part): Attachment
    suspend fun saveEvent(event: Event)
    suspend fun getEventRequest(id: Int): Event
    suspend fun getUserById(id: Int): User
}
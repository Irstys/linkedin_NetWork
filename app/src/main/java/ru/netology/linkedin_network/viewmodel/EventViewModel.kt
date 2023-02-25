package ru.netology.linkedin_network.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import ru.netology.linkedin_network.auth.AppAuth
import ru.netology.linkedin_network.dto.*
import ru.netology.linkedin_network.enumeration.AttachmentType
import ru.netology.linkedin_network.enumeration.EventType
import ru.netology.linkedin_network.model.FeedModelState
import ru.netology.linkedin_network.model.MediaModel
import ru.netology.linkedin_network.repository.event.EventRepository
import ru.netology.linkedin_network.utils.SingleLiveEvent
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import ru.netology.linkedin_network.dto.Event.Companion.emptyEvent
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt

val speakers = mutableListOf<User>()

private val noMedia = MediaModel()

@ExperimentalCoroutinesApi
@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    appAuth: AppAuth
) : ViewModel() {

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _Event = MutableLiveData<Event>()
    val Event: LiveData<Event>
        get() = _Event

    private val _Eventd = SingleLiveEvent<Unit>()
    val Eventd: LiveData<Unit>
        get() = _Eventd
    private val emptyCords = Point()
    private val _cords = MutableLiveData<Point>(emptyCords)
    val cords: LiveData<Point>
        get() = _cords


    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media

    val eventUsersData: LiveData<List<UserPreview>> = repository.eventUsersData

    var isEventIntent = false

    val newEvent: MutableLiveData<Event> = MutableLiveData(emptyEvent)
    val usersList: MutableLiveData<List<User>> = MutableLiveData()
    val speakerslist: LiveData<List<UserPreview>> = repository.eventSpeakersData
    val participatesList: LiveData<List<UserPreview>> = repository.eventParticipatesData
    val likersList: LiveData<List<UserPreview>> = repository.eventLikersData
    val speakersData: MutableLiveData<MutableList<User>> = MutableLiveData()


    val data: Flow<PagingData<Event>> = appAuth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            val cached = repository.data.cachedIn(viewModelScope)
            cached.map { pagingData ->
                pagingData.map {
                    it.copy(ownedByMe = it.authorId!!.toInt() == myId)
                }

            }
        }

    fun getEventUsersList(event: Event) {
        viewModelScope.launch {
            try {
                repository.getEventUsersList(event)
                _dataState.value = FeedModelState(loading = false)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(loading = true)
            }
        }
    }
    fun getSpeakers(event: Event) {
        viewModelScope.launch {
            try {
                repository.getSpeakers(event)
                _dataState.value = FeedModelState(loading = false)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(loading = true)
            }
        }
    }
    fun getLikers(event: Event) {
        viewModelScope.launch {
            try {
                repository.getLikers(event)
                _dataState.value = FeedModelState(loading = false)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(loading = true)
            }
        }
    }

    fun getParticipates(event: Event) {
        viewModelScope.launch {
            try {
                repository.getParticipates(event)
                _dataState.value = FeedModelState(loading = false)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(loading = true)
            }
        }
    }

    fun removeById(id: Int) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun likeEvent(event: Event){
        viewModelScope.launch {
            try {
                _Event.postValue(repository.likeEvent(event))
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }


    fun join(event: Event) {
        viewModelScope.launch {
            try {
                _Event.postValue(repository.join(event))
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getEventRequest(id: Int) {
        speakersData.postValue(speakers)
        viewModelScope.launch {
            try {
                newEvent.value = repository.getEventRequest(id)
                newEvent.value?.speakerIds?.forEach {
                    speakersData.value!!.add(repository.getUserById(it))
                }
                _dataState.value = FeedModelState(error = false)
            } catch (e: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getUsers() {
        speakersData.postValue(speakers)
        viewModelScope.launch {
            try {
                usersList.value = repository.getUsers()
                usersList.value?.forEach { user ->
                    newEvent.value?.speakerIds?.forEach {
                        if (user.id == it) {
                            user.isChecked = true
                        }
                    }
                }
                _dataState.value = FeedModelState(error = false)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun addSpeakerIds() {
        speakersData.postValue(speakers)
        val listChecked = mutableListOf<Int>()
        val speakersUserList = mutableListOf<User>()
        usersList.value?.forEach { user ->
            if (user.isChecked) {
                listChecked.add(user.id)
                speakersUserList.add(user)
            }
        }
        speakersData.postValue(speakersUserList)
        newEvent.value = newEvent.value?.copy(speakerIds = listChecked)
    }

    fun check(id: Int) {
        usersList.value?.forEach {
            if (it.id == id) {
                it.isChecked = true
            }
        }
    }

    fun unCheck(id: Int) {
        usersList.value?.forEach {
            if (it.id == id) {
                it.isChecked = false
            }
        }
    }

        fun addcoordinates(point: Point) {
            _cords.value=point
            val coordinates = Coordinates(
                ((point.latitude * 1000000.0).roundToInt() / 1000000.0).toString(),
                ((point.longitude * 1000000.0).roundToInt() / 1000000.0).toString()
            )
            newEvent.value = newEvent.value?.copy(coordinates = coordinates)
            isEventIntent = false
        }

    fun addLink(link: String) {
        if (link != "") {
            newEvent.value = newEvent.value?.copy(link = link)
        } else {
            newEvent.value = newEvent.value?.copy(link = null)
        }
    }

    fun changeMedia(uri: Uri?, file: File?, type: AttachmentType?) {
        _media.value = MediaModel(uri, file, type)
    }

    fun addMediaToEvent(
        type: AttachmentType,
        file: MultipartBody.Part
    ) {
        viewModelScope.launch {
            try {
                val attachment = repository.addMediaToEvent(type, file)
                newEvent.value = newEvent.value?.copy(attachment = attachment)
                _dataState.value = FeedModelState(error = false)
            } catch (e: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun addDateAndTime(dateAndTime: String) {
        newEvent.value = newEvent.value?.copy(datetime = dateAndTime)
    }

    fun addEventType() {
        val type = when (newEvent.value?.type) {
            EventType.OFFLINE -> EventType.ONLINE
            else -> EventType.OFFLINE
        }
        newEvent.value = newEvent.value?.copy(type = type)
    }

    fun saveEvent(event: Event) {
        viewModelScope.launch {
            try {
                repository.saveEvent(event)
                _dataState.value = FeedModelState(error = false)
                deleteEditEvent()
                _Eventd.value = Unit
            } catch (e: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun deleteEditEvent() {
        newEvent.postValue(emptyEvent)
        speakers.clear()
        speakersData.postValue(speakers)
    }
}
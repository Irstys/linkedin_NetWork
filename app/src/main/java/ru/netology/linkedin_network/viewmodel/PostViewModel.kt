package ru.netology.linkedin_network.viewmodel

import android.net.Uri
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import ru.netology.linkedin_network.auth.AppAuth
import ru.netology.linkedin_network.dto.*
import ru.netology.linkedin_network.enumeration.AttachmentType
import ru.netology.linkedin_network.model.FeedModelState
import ru.netology.linkedin_network.model.MediaModel
import ru.netology.linkedin_network.repository.PostRepository
import ru.netology.linkedin_network.utils.SingleLiveEvent
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import ru.netology.linkedin_network.dto.Post.Companion.emptyPost
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt


private val mentions = mutableListOf<User>()

private val noMedia = MediaModel()

@ExperimentalCoroutinesApi
@HiltViewModel
class PostViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _Post = MutableLiveData<Post>()
    val Post: LiveData<Post>
        get() = _Post

    private val _Postd = SingleLiveEvent<Unit>()
    val Postd: LiveData<Unit>
        get() = _Postd

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media

    val postUsersData: LiveData<List<UserPreview>> = repository.postUsersData
    val mentorslist: LiveData<List<UserPreview>> = repository.postMentionsData
    val likersList: LiveData<List<UserPreview>> = repository.postLikersData

    var isPostIntent = false

    val newPost: MutableLiveData<Post> = MutableLiveData(emptyPost)
    val usersList: MutableLiveData<List<User>> = MutableLiveData()
    val mentionsData: MutableLiveData<MutableList<User>> = MutableLiveData()


    private val cached
        get() = repository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<Post>> = appAuth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.filter { post ->
                     !hidePosts.contains(post.id)
                    }
                pagingData.map {
                    it.copy(ownedByMe = it.authorId == myId)
                }

            }
        }

    val hidePosts = mutableSetOf<Int>()

    fun hidePost(id:Int) {
        hidePosts.add(id)
    }
    fun getLikedAndMentionedUsersList(post: Post) {
        viewModelScope.launch {
            try {
                repository.getLikedAndMentionedUsersList(post)
                _dataState.value = FeedModelState(loading = false)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(loading = true)
            }
        }
    }

    fun getMentionedUsersList(post: Post) {
        viewModelScope.launch {
            try {
                repository.getMentions(post)
                _dataState.value = FeedModelState(loading = false)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(loading = true)
            }
        }
    }
    fun getLikedUsersList(post: Post) {
        viewModelScope.launch {
            try {
                repository.getLikers(post)
                _dataState.value = FeedModelState(loading = false)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(loading = true)
            }
        }
    }
    fun removePostById(id: Int) {
        viewModelScope.launch {
            try {
                repository.removePostById(id)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun likePostById(id: Int) {
        viewModelScope.launch {
            try {
                _Post.postValue(repository.likePostById(id))
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun dislikePostById(id: Int) {
        viewModelScope.launch {
            try {
                _Post.postValue(repository.dislikePostById(id))
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getPostRequest(id: Int) {
        mentionsData.postValue(mentions)
        viewModelScope.launch {
            try {
                newPost.value = repository.getPostRequest(id)
                newPost.value?.mentionIds?.forEach {
                    mentionsData.value!!.add(repository.getUserById(it))
                }
                _dataState.value = FeedModelState(error = false)
            } catch (e: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getUsers() {
        mentionsData.postValue(mentions)
        viewModelScope.launch {
            try {
                usersList.value = repository.getUsers()
                usersList.value?.forEach { user ->
                    newPost.value?.mentionIds?.forEach {
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

    fun addMentionIds() {
        mentionsData.postValue(mentions)
        val listChecked = mutableListOf<Int>()
        val mentionsUserList = mutableListOf<User>()
        usersList.value?.forEach { user ->
            if (user.isChecked) {
                listChecked.add(user.id)
                mentionsUserList.add(user)
            }
        }
        mentionsData.postValue(mentionsUserList)
        newPost.value = newPost.value?.copy(mentionIds = listChecked)
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
        val coordinates = Coordinates(
            ((point.latitude * 1000000.0).roundToInt() / 1000000.0).toString(),
            ((point.longitude * 1000000.0).roundToInt() / 1000000.0).toString()
        )
        newPost.value = newPost.value?.copy(coordinates = coordinates)
        isPostIntent = false
    }

    fun addLink(link: String) {
        if (link != "") {
            newPost.value = newPost.value?.copy(link = link)
        } else {
            newPost.value = newPost.value?.copy(link = null)
        }
    }

    fun changeMedia(uri: Uri?, file: File?, type: AttachmentType?) {
        _media.value = MediaModel(uri, file, type)
    }

    fun savePost(content: String) {
        newPost.value = newPost.value?.copy(content = content)
        val post = newPost.value!!
        viewModelScope.launch {
            try {
                repository.savePost(post)
                _dataState.value = FeedModelState(error = false)
                deleteEditPost()
                _Postd.value = Unit
            } catch (e: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun addMediaToPost(
        type: AttachmentType,
        file: MultipartBody.Part
    ) {
        viewModelScope.launch {
            try {
                val attachment = repository.addMediaToPost(type, file)
                newPost.value = newPost.value?.copy(attachment = attachment)
                _dataState.value = FeedModelState(error = false)
            } catch (e: RuntimeException) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun deleteEditPost() {
        newPost.value = emptyPost
        mentions.clear()
        mentionsData.postValue(mentions)
    }
}
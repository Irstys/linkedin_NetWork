package ru.netology.linkedin_network.viewmodel

import android.net.Uri
import android.util.Log
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
import ru.netology.linkedin_network.repository.post.PostRepository
import ru.netology.linkedin_network.utils.SingleLiveEvent
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import ru.netology.linkedin_network.dto.Post.Companion.emptyPost
import java.io.File
import kotlin.random.Random
import javax.inject.Inject
import kotlin.math.roundToInt

private val adUrl = "https://ik.imagekit.io/jwudrxfj5ek/073d2310-df37-41e7-8d99-2b8d9c4cd8e5._AwQCvKQES.png"
private val mentions = mutableListOf<User>()

private val noMedia = MediaModel()

@ExperimentalCoroutinesApi
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _Post = MutableLiveData<Post>()
    val Post: LiveData<Post>
        get() = _Post

    private val _editedPost = SingleLiveEvent<Unit>()
    val editedPost: LiveData<Unit>
        get() = _editedPost

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media

    val postUsersData: LiveData<List<UserPreview>> = repository.postUsersData
    val mentorslist: LiveData<List<UserPreview>> = repository.postMentionsData
    val likersList: LiveData<List<UserPreview>> = repository.postLikersData
    private val emptyCords = Point()
    var isPostIntent = false

    private val _cords = MutableLiveData<Point>(emptyCords)
    val cords: LiveData<Point>
        get() = _cords

    val newPost: MutableLiveData<Post> = MutableLiveData(emptyPost)
    val usersList: MutableLiveData<List<User>> = MutableLiveData()
    val mentionsData: MutableLiveData<MutableList<User>> = MutableLiveData()


    private val cached
        get() = repository.data.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> = appAuth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.insertSeparators(
                    generator = { before, _ ->
                        if (before?.id?.rem(5) == 0) {
                            Ad(Random.nextInt(),adUrl)
                        } else {
                            null
                        }
                    })
                pagingData.map {
                    it.copy(ownedByMe = it.authorId == myId)
                }

            }
        }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadUserWall(id: Int): Flow<PagingData<FeedItem>> = appAuth
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.loadUserWall(id).map { pagingData ->
                pagingData.insertSeparators(
                    generator = { before, _ ->
                        if (before?.id?.rem(5) == 0) {
                            Ad(Random.nextInt(),adUrl)
                        } else {
                            null
                        }
                    })
                pagingData.map { post ->
                    post.copy(
                        ownedByMe = post.authorId == myId,
                        likedByMe = post.likeOwnerIds.contains(myId)
                    )
                }
            }
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

    fun likePost(post: Post) {
        viewModelScope.launch {
            try {
                _Post.postValue(repository.likePost(post))
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }


    fun getPostById(id: Int) {
        mentionsData.postValue(mentions)
        viewModelScope.launch {
            try {
                newPost.value = repository.getPostById(id)
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
        _cords.value=point
        val coordinates = Coordinates(
            ((point.latitude * 1000000.0).roundToInt() / 1000000.0).toString(),
            ((point.longitude * 1000000.0).roundToInt() / 1000000.0).toString()
        )
        this.newPost.value = newPost.value?.copy(coordinates = coordinates)
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
        cords.value?.let { addcoordinates(it) }
        Log.d("Post", newPost.value.toString())
        newPost.value = newPost.value?.copy(content = content)
        Log.d("Post1", newPost.value.toString())
        val post = newPost.value!!
        viewModelScope.launch {
            try {
                repository.savePost(post)
                Log.d("Post1", newPost.value.toString())
                Log.d("Post1", post.toString())
                _dataState.value = FeedModelState(error = false)
                deleteEditPost()
                _editedPost.value = Unit
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
        _cords.value = emptyCords
        mentionsData.postValue(mentions)
    }

}
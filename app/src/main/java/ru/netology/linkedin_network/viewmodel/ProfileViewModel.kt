package ru.netology.linkedin_network.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.netology.linkedin_network.auth.AppAuth
import ru.netology.linkedin_network.dto.Job
import ru.netology.linkedin_network.dto.User
import ru.netology.linkedin_network.model.FeedModelState
import ru.netology.linkedin_network.repository.job.JobRepository
import ru.netology.linkedin_network.repository.user.UserRepository
import ru.netology.linkedin_network.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

val editedJob = Job(
    id = 0,
    name = "",
    position = "",
    start = "",
    finish = null,
    link = null
)

@ExperimentalCoroutinesApi
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val jobRepository: JobRepository,
    appAuth: AppAuth
) : ViewModel() {

    val myId: Int = appAuth.authStateFlow.value.id


    val newJob: MutableLiveData<Job> = MutableLiveData(editedJob)

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    val data: MutableLiveData<List<User>> = userRepository.data
    val userData: MutableLiveData<User> = userRepository.userData
    val jobData: MutableLiveData<List<Job>> = jobRepository.data


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    fun getAllUsers() {
        viewModelScope.launch {
            try {
                userRepository.getAllUsers()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getUserById(id: Int) {
        viewModelScope.launch {
            try {
                userRepository.getUserById(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getUserJobs(id: Int) {
        viewModelScope.launch {
            try {
                jobRepository.getUserJobs(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun saveJob(job: Job) {
        viewModelScope.launch {
            try {
                jobRepository.saveJob(job)
                _dataState.value = FeedModelState(error = false)
                deleteEditJob()
                _jobCreated.value = Unit
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun removeJobById(id: Int) {
        viewModelScope.launch {
            try {
                jobRepository.removeJobById(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun getMyJobs() {
        viewModelScope.launch {
            try {
                jobRepository.getMyJobs()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun deleteEditJob() {
        newJob.postValue(editedJob)
    }

    fun addStartDate(date: String) {
        newJob.value = newJob.value?.copy(start = date)
    }

    fun addEndDate(date: String) {
        newJob.value = newJob.value?.copy(finish = date)
    }
}
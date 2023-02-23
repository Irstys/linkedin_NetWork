package ru.netology.linkedin_network.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.netology.linkedin_network.auth.AppAuth
import ru.netology.linkedin_network.dto.MediaUpload
import ru.netology.linkedin_network.enumeration.AttachmentType
import ru.netology.linkedin_network.model.MediaModel
import ru.netology.linkedin_network.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private val noAvatar = MediaModel()

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val auth: AppAuth
) : ViewModel() {

    private val _avatar = MutableLiveData(noAvatar)
    val avatar: LiveData<MediaModel>
        get() = _avatar

    fun registerUser(login: String, pass: String, name: String) = viewModelScope.launch {
        val response = repository.registerUser(login, pass, name)
        response.token?.let {
            auth.setAuth(response.id, response.token, response.avatar, response.name)
        }
    }

    fun registerUserWithAvatar(login: String, pass: String, name: String, media: MediaUpload) =
        viewModelScope.launch {
            val response = repository.registerUserWithAvatar(login, pass, name, media)
            response.token?.let {
                auth.setAuth(response.id, response.token, response.avatar, response.name)
            }
        }

    fun changeAvatar(uri: Uri?, file: File?) {
        _avatar.value = MediaModel(uri, file, AttachmentType.IMAGE)
    }
}
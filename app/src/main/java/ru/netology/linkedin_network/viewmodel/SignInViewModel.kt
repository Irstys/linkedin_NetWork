package ru.netology.linkedin_network.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.netology.linkedin_network.auth.AppAuth
import ru.netology.linkedin_network.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val auth: AppAuth
) : ViewModel() {

    fun signIn(login: String, password: String) = viewModelScope.launch {
        val response = repository.signIn(login, password)
        response.token?.let {
            auth.setAuth(response.id, response.token, response.avatar, response.name)
        }
    }
}
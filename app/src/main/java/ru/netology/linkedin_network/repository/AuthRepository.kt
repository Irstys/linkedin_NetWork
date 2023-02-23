package ru.netology.linkedin_network.repository

import ru.netology.linkedin_network.auth.AuthState
import ru.netology.linkedin_network.dto.MediaUpload

interface AuthRepository {
    suspend fun signIn(login: String, pass: String): AuthState
    suspend fun registerUser(login: String, pass: String, name: String): AuthState
    suspend fun registerUserWithAvatar(
        login: String,
        pass: String,
        name: String,
        media: MediaUpload
    ): AuthState
}
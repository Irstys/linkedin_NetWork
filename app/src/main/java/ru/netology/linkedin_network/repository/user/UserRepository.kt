package ru.netology.linkedin_network.repository.user

import androidx.lifecycle.MutableLiveData
import ru.netology.linkedin_network.dto.User

interface UserRepository {
    val data: MutableLiveData<List<User>>
    val userData: MutableLiveData<User>
    suspend fun getAllUsers()
    suspend fun getUserById(id: Int)
}
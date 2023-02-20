package ru.netology.linkedin_network.dto

import okhttp3.MultipartBody

data class Registration(
    val login: String,
    val password: String,
    val name: String,
    val file: MultipartBody.Part?
)

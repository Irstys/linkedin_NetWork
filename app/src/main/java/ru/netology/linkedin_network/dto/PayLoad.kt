package ru.netology.linkedin_network.dto

data class Payload(
    val liked: Boolean? = null,
    val join: Boolean? = null,
    val content: String? = null,
)
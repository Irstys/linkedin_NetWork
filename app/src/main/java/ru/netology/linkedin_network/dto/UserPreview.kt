package ru.netology.linkedin_network.dto

data class UserPreview(
    val id: Int = 0,
    val login: String? = null,
    val name: String = "",
    val avatar: String? = null,
    var isLiked: Boolean = false,
    var isMentioned: Boolean = false,
    var isParticipating: Boolean = false,
    var isSpeaker: Boolean = false,
)
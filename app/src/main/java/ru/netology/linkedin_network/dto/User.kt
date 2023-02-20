package ru.netology.linkedin_network.dto

data class User(
    val id: Int = 0,
    val login: String? = null,
    val name: String = "",
    val avatar: String? = null,
    var isChecked: Boolean = false,
    var isLiked: Boolean = false,
    var isMentioned: Boolean = false,
    var isParticipating: Boolean = false,
    var isSpeaker: Boolean = false,
) {
    companion object {
        val nuller = User(
            id = 0,
        )
    }
}
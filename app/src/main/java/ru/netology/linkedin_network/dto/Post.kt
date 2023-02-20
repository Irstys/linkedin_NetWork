package ru.netology.linkedin_network.dto

import com.google.gson.annotations.SerializedName

data class Post(
    override val id: Int,
    val authorId: Int? =null,
    val author: String? =null,
    val authorAvatar: String? =null,
    val authorJob: String? =null,
    val content: String,
    val published: String? =null,
    /*координаты события*/
    @SerializedName("emptyEvent")
    val coordinates: Coordinates? =null,
    val link: String? = null,
    /*кто лайкнул*/
    val likeOwnerIds:  List<Int> = emptyList(),
    /*кто упоминается*/
    val mentionIds: List<Int> = emptyList(),
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    val users: Map<Int, User> = emptyMap(),
) : FeedItem
{
    companion object {
        val emptyPost = Post(
            id = 0,
            content = "",
            coordinates = null,
            link = null,
            attachment = null,
            mentionIds = listOf()
        )
    }
}
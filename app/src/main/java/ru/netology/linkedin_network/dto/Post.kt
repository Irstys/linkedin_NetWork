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
    @SerializedName("coords")
    val coordinates: Coordinates? =null,
    val link: String? = null,
    /*кто лайкнул*/
    val likeOwnerIds:  List<Int>? = null,
    /*кто упоминается*/
    val mentionIds: List<Int>? = null,
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    val users: Map<Int, User>? = null,
) : FeedItem
{
    companion object {
        val emptyPost = Post(
            id = 0,
            author = User.nuller.name,
            authorId = User.nuller.id,
            authorAvatar = User.nuller.avatar,
            content = "",
            published = "2021-08-17T16:46:58.887547Z",
            mentionedMe = false,
            likedByMe = false,
            ownedByMe = false,
            users = null
        )
    }
}
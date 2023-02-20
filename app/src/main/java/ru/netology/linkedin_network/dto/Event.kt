package ru.netology.linkedin_network.dto

import com.google.gson.annotations.SerializedName
import ru.netology.linkedin_network.dto.User.Companion.nuller
import ru.netology.linkedin_network.enumeration.EventType

data class Event(
    override val id: Int,
    val authorId: Int? = null,
    val author: String? = null,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String? = null,
    /*дата проведения*/
    val datetime: String,
    val published: String? = null,
    /*координаты события*/
    @SerializedName("coords")
    val coordinates: Coordinates? =null,
    val type: EventType? = EventType.OFFLINE,
    /*кто лайкнул*/
    val likeOwnerIds:  List<Int> = emptyList(),
    val likedByMe: Boolean =false,
    /*кто спикер*/
    val speakerIds:  List<Int> = emptyList(),
    /*кто участвует*/
    val participantsIds:  List<Int> = emptyList(),
    val participatedByMe: Boolean=false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean=false,
    val users: Map<Int, User>?=null
) : FeedItem {
    companion object {
        val emptyEvent = Event(
            id = 0,
            author = nuller.name,
            authorId = nuller.id,
            content = "",
            datetime = "2021-08-17T16:46:58.887547Z",
            published ="2021-08-17T16:46:58.887547Z",
            type = EventType.ONLINE,
            authorAvatar=nuller.avatar,
            users =null
        )
    }
}


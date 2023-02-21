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
    val datetime: String? = null,
    val published: String? = null,
    /*координаты события*/
    @SerializedName("emptyEvent")
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
    val users: Map<Int, UserPreview> = emptyMap(),
) : FeedItem {
    companion object {
        val emptyEvent = Event(
            id = 0,
            content = "",
            datetime = null,
            coordinates = null,
            type = EventType.OFFLINE,
            attachment = null,
            link = null,
            speakerIds = listOf()
        )
    }
}


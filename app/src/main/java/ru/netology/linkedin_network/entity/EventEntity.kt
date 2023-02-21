package ru.netology.linkedin_network.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.linkedin_network.dao.Converters
import ru.netology.linkedin_network.dto.Event
import ru.netology.linkedin_network.dto.User
import ru.netology.linkedin_network.dto.UserPreview

@Entity(tableName = "events")
@TypeConverters(Converters::class)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val authorId: Int? = null,
    val author: String? = null,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String? = null,
    val datetime: String? = null,
    val published: String? = null,
    @Embedded
    val coordinates: CoordinatesEmbeddable? =null,
    @Embedded
    val type: EventTypeEmbeddable,
    val likeOwnerIds:  List<Int> = emptyList(),
    val likedByMe: Boolean =false,
    val speakerIds:  List<Int> = emptyList(),
    val participantsIds:  List<Int> = emptyList(),
    val participatedByMe: Boolean=false,
    @Embedded
    val attachment: AttachmentEmbeddable? = null,
    val link: String? = null,
    val ownedByMe: Boolean=false,
    val users: Map<Int, UserPreview> = emptyMap(),
) {

    fun toDto() = Event(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        authorJob=authorJob,
        content = content,
        datetime = datetime,
        published = published,
        coordinates = coordinates?.toDto(),
        type = type.toDto(),
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        speakerIds = speakerIds,
        participantsIds = participantsIds,
        participatedByMe = participatedByMe,
        attachment = attachment?.toDto(),
        link = link,
        ownedByMe = ownedByMe,
        users=users,
    )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.datetime,
                dto.published,
                CoordinatesEmbeddable.fromDto(dto.coordinates),
                EventTypeEmbeddable.fromDto(dto.type),
                dto.likeOwnerIds,
                dto.likedByMe,
                dto.speakerIds,
                dto.participantsIds,
                dto.participatedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
                dto.link,
                dto.ownedByMe,
                dto.users,
            )

    }

}
fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)


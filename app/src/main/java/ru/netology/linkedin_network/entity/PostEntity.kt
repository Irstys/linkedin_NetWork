package ru.netology.linkedin_network.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.linkedin_network.dao.Converters
import ru.netology.linkedin_network.dto.Post
import ru.netology.linkedin_network.dto.User
import ru.netology.linkedin_network.dto.UserPreview

@Entity(tableName = "posts")
@TypeConverters(Converters::class)
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val authorId: Int? =null,
    val author: String? =null,
    val authorAvatar: String? =null,
    val authorJob: String? =null,
    val content: String,
    val published: String? =null,
    @Embedded
    val coordinates: CoordinatesEmbeddable? = null,
    val link: String? = null,
    val likeOwnerIds: List<Int> = emptyList(),
    val mentionIds:  List<Int> = emptyList(),
    val mentionedMe: Boolean,
    val likedByMe: Boolean,
    @TypeConverters(Converters::class)
    @Embedded
    val attachment: AttachmentEmbeddable? = null,
    val ownedByMe: Boolean,
    @TypeConverters(Converters::class)
    val users: Map<Int, UserPreview> = emptyMap(),

    ){
    fun toDto()= Post(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        authorJob=authorJob,
        content = content,
        published = published,
        coordinates = coordinates?.toDto(),
        link = link,
        likeOwnerIds = likeOwnerIds,
        mentionIds = mentionIds,
        mentionedMe = mentionedMe,
        likedByMe = likedByMe,
        attachment = attachment?.toDto(),
        ownedByMe = ownedByMe,
        users=users,
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.published,
                CoordinatesEmbeddable.fromDto(dto.coordinates),
                dto.link,
                dto.likeOwnerIds,
                dto.mentionIds,
                dto.mentionedMe,
                dto.likedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
                dto.ownedByMe,
                dto.users,
            )

    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)




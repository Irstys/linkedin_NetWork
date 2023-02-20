package ru.netology.linkedin_network.dto

import ru.netology.linkedin_network.enumeration.AttachmentType

data class Attachment(
    val url: String,
    val type: AttachmentType,
)
{
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment) = dto.let {
            Attachment(it.url, it.type)
        }
    }
}
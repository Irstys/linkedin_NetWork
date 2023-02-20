package ru.netology.linkedin_network.entity

import ru.netology.linkedin_network.dto.Attachment
import ru.netology.linkedin_network.enumeration.AttachmentType

data class AttachmentEmbeddable(
    val uri: String,
    val type: AttachmentType,
) {
    fun toDto() = Attachment(uri, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}
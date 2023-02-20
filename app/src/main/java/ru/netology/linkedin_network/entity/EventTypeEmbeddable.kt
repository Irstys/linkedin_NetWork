package ru.netology.linkedin_network.entity

import ru.netology.linkedin_network.enumeration.EventType

data class EventTypeEmbeddable(
    val eventType: String
) {
    fun toDto() = EventType.valueOf(eventType.toString())

    companion object {
        fun fromDto(dto: EventType?) = EventTypeEmbeddable(dto!!.name)
    }
}
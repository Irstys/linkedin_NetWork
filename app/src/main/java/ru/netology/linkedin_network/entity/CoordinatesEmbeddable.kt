package ru.netology.linkedin_network.entity

import ru.netology.linkedin_network.dto.Coordinates

data class CoordinatesEmbeddable(
    val latitude: String,
    val longitude: String
) {
    fun toDto() = Coordinates(latitude, longitude)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordinatesEmbeddable(it.latitude, it.longitude)
        }
    }
}

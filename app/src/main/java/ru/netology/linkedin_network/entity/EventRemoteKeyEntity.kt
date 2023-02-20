package ru.netology.linkedin_network.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.linkedin_network.enumeration.KeyType

@Entity
data class EventRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Int,
)

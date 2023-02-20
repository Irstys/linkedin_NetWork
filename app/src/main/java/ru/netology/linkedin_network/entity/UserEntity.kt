package ru.netology.linkedin_network.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.linkedin_network.dto.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val login: String? = "",
    val name: String = "",
    val avatar: String? = "",
    var isChecked: Boolean = false
) {

    fun toDto() = User(
        id = id,
        login = login,
        name = name,
        avatar = avatar,
        isChecked=isChecked
    )

    companion object {
        fun fromDto(dto: User) =
            UserEntity(dto.id, dto.login, dto.name, dto.avatar, dto.isChecked)
    }
}

fun List<UserEntity>.toDto(): List<User> = map(UserEntity::toDto)
fun List<User>.toEntity(): List<UserEntity> = map(UserEntity::fromDto)
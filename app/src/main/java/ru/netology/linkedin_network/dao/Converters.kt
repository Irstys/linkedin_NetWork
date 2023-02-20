package ru.netology.linkedin_network.dao

import androidx.room.Dao
import androidx.room.TypeConverter
import ru.netology.linkedin_network.dto.Coordinates
import ru.netology.linkedin_network.dto.User
import ru.netology.linkedin_network.enumeration.AttachmentType
import ru.netology.linkedin_network.enumeration.EventType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Dao
class Converters {
    @TypeConverter
    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType) = value.name

    @TypeConverter
    fun fromListInt(list: List<Int?>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toListInt(string: String?): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(string, type)
    }

    @TypeConverter
    fun fromUsers(map: Map<Int, User>): String {
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toUsers(string: String): Map<Int, User> {
        val maptype = object : TypeToken<Map<Int, User>>() {}.type
        return Gson().fromJson(string, maptype)
    }

    @TypeConverter
    fun fromEventType(value: EventType) = value.name

    @TypeConverter
    fun toEventType(value: String) = enumValueOf<EventType>(value)

    @TypeConverter
    fun coordinatesToJson(coordinates: Coordinates?): String? {
        return if (coordinates == null) {
            null
        } else {
            Gson().toJson(coordinates)
        }
    }
    @TypeConverter
    fun jsonToCoordinates(json: String?): Coordinates? {
        return if (json.isNullOrEmpty()) {
            null
        } else {
            val type = object : TypeToken<Coordinates>() {}.type
            Gson().fromJson(json, type)
        }
    }
}
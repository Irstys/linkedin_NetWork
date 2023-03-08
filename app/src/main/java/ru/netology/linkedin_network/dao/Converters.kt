package ru.netology.linkedin_network.dao

import androidx.room.Dao
import androidx.room.TypeConverter
import ru.netology.linkedin_network.dto.Coordinates
import ru.netology.linkedin_network.enumeration.AttachmentType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.linkedin_network.dto.UserPreview

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
    fun fromUsers(map: Map<Int, UserPreview>): String {
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toUsers(string: String): Map<Int, UserPreview> {
        val maptype = object : TypeToken<Map<Int, UserPreview>>() {}.type
        return Gson().fromJson(string, maptype)
    }
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
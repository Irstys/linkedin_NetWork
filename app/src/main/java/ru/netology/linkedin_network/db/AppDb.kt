package ru.netology.linkedin_network.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.linkedin_network.dao.*
import ru.netology.linkedin_network.entity.*

@Database(
    entities = [PostEntity::class,
        EventEntity::class,
        EventRemoteKeyEntity::class,
        PostRemoteKeyEntity::class,
        JobEntity::class,
        UserEntity::class], version = 4
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun PostDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun jobDao(): JobDao
    abstract fun userDao(): UserDao
}
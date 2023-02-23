package ru.netology.linkedin_network.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface EventRepositoryModule {
    @Binds
    @Singleton
    fun bindEventRepository(impl: EventRepositoryImpl): EventRepository
}
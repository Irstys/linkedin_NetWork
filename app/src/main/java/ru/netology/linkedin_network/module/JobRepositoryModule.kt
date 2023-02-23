package ru.netology.linkedin_network.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.linkedin_network.repository.job.JobRepository
import ru.netology.linkedin_network.repository.job.JobRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface JobRepositoryModule {
    @Binds
    @Singleton
    fun bindJobRepository(impl: JobRepositoryImpl): JobRepository
}
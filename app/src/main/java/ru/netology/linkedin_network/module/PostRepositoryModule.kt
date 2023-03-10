package ru.netology.linkedin_network.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.linkedin_network.repository.post.PostRepository
import ru.netology.linkedin_network.repository.post.PostRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface PostRepositoryModule {
    @Binds
    @Singleton
    fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
}
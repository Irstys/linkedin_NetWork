package ru.netology.linkedin_network.dto

data class Ad(
    override val id: Int,
    val name: String
) : FeedItem
package ru.netology.linkedin_network.dto

import ru.netology.linkedin_network.dto.FeedItem

data class TextItemSeparator(
    override val id: Int,
    val text: String,
) : FeedItem
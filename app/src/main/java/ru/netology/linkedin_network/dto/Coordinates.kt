package ru.netology.linkedin_network.dto

import com.google.gson.annotations.SerializedName

data class Coordinates(
    @SerializedName("lat")
    val latitude: String,
    @SerializedName("long")
    val longitude: String
)

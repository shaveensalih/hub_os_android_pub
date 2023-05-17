package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class Advertiser(
    val id: Int,
    val name: String,
    @SerializedName("broadcast_message") val broadcastMessage: String?,
)

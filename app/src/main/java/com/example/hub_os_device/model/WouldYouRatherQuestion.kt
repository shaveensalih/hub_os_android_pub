package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class WouldYouRatherResult(val id: String, @SerializedName("data") val question: String)

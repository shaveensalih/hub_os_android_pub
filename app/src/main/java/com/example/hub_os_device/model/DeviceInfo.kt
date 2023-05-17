package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class DeviceInfo(
    val id: String,
    val tenant: Advertiser,
    @SerializedName("running_ads") val advertisements: List<Advertisement>?,
    val establishment: Establishment?,
)

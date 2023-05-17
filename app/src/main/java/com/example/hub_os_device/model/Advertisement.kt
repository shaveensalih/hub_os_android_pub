package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class Advertisement(
    val id: String,
    @SerializedName("primary_images") val primaryImgUrls: List<String>?,
    @SerializedName("secondary_image") val secondaryImgUrl: String,
    @SerializedName("banner_image") val bannerImgUrl: String,
    @SerializedName("video_file") val videoUrl: String?,
    val color: String,
    val advertiser: Advertiser
)

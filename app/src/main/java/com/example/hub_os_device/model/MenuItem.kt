package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class MenuItem(
    val id: String,
    val name: String,
    @SerializedName("description") val desc: String,
    val price: Double,
    val tags: List<String>?,
    @SerializedName("options") val extras: List<MenuItem>?,
    @SerializedName("menu_category_id") val categoryId: Int?,
    @SerializedName("self_serviceable") val serviceable: Int,
    @SerializedName("image_file") val imageUrls: List<ImageObject>?,
)

data class ImageObject(
    @SerializedName("original_url") val originalImage: String?,
    @SerializedName("preview_url") val previewImage: String?,
    @SerializedName("optimized_url") val optimizedImage: String?,
)

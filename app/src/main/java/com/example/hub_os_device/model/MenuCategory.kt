package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class MenuCategory(
    val id: String,
    val name: String,
    val description: String?,
    val items: List<MenuItem>,
    @SerializedName("menu_category_id") val menuCategoryId: Int?
)

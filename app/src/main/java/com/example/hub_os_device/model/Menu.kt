package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class Menu(
    val food: List<MenuCategory>,
    val drinks: List<MenuCategory>,
    val shisha: List<MenuCategory>,
    val other: List<MenuCategory>,
)

package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class BillListItem(
    val id: Int,
    val subtotal: Double,
    val note: String?,
    val gifted: Int,
    @SerializedName("created_at") val timeOrdered: String,
    @SerializedName("options") val extras: List<OptionItem>?,
    @SerializedName("menu_item") val menuItem: MenuItem,
)

data class OptionItem(
    @SerializedName("optionId") val id: Int,
    @SerializedName("optionName") val name: String,
    @SerializedName("optionPrice") val price: Double,
)
package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class Discount(
    val id: Int,
    val type: DiscountType,
    @SerializedName("discount_percentage") val percentage: Int,
    @SerializedName("categories") val categoryIds: List<MenuCategory>?,
    @SerializedName("starting_time") val startingTime: String?,
    @SerializedName("ending_time") val endingTime: String?,
)

enum class DiscountType(val value: String) {
    @SerializedName("normal")
    NORMAL("normal"),

    @SerializedName("category-based")
    CATEGORY("category-based"),

    @SerializedName("time-based")
    TIME("time-based"),

    @SerializedName("time-category-based")
    TIME_CATEGORY("time-category-based"),
}

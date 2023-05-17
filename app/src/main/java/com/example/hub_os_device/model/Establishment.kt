package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class Establishment(
    val id: String,
    val name: String,
    @SerializedName("menu_url") val qrCode: String? = null,
    @SerializedName("pin_code") val passcode: String,
    @SerializedName("wifi_ssid") val wifiSSID: String?,
    @SerializedName("wifi_password") val wifiCode: String?,
    @SerializedName("dineos_integration") val dineosIntegration: Boolean,
    @SerializedName("service_charge_percentage") val servicePercentage: String,
    @SerializedName("vat_charge_percentage") val vatPercentage: String,
    @SerializedName("table_count") val tableCount: Int?,
    var menu: List<MenuSection>?,
)

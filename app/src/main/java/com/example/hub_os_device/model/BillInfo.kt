package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class BillInfo(
    val id: Int,
    val status: BillStatus,
    var service: Int?,
    var vat: Int?,
    val discount: Discount?,
    val items: List<BillListItem>,
    ) {
    val hasDiscount get() = status == BillStatus.AWAITING_PAYMENT && discount != null
    val hasVat get() = status == BillStatus.AWAITING_PAYMENT && discount != null
}

enum class BillStatus(val value: String) {
    @SerializedName("active")
    ACTIVE("active"),

    @SerializedName("awaiting_payment")
    AWAITING_PAYMENT("awaiting_payment"),
}

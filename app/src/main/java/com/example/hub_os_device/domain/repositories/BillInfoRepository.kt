package com.example.hub_os_device.domain.repositories

import com.example.hub_os_device.model.BillInfo
import kotlinx.coroutines.flow.SharedFlow

interface BillInfoRepository {
    val billInfoFlow: SharedFlow<Result<BillInfo?>>
    suspend fun getBill(id: Int, tableNum: String)
}
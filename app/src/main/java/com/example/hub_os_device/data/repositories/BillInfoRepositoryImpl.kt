package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.data.remote.BillInfoRemoteDataSource
import com.example.hub_os_device.domain.repositories.BillInfoRepository
import com.example.hub_os_device.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class BillInfoRepositoryImpl @Inject constructor(private val billInfoRemoteDataSource: BillInfoRemoteDataSource) :
    BillInfoRepository {

    private val _billInfo = MutableSharedFlow<Result<BillInfo?>>(replay = 1)
    override val billInfoFlow: SharedFlow<Result<BillInfo?>> = _billInfo.asSharedFlow()

    override suspend fun getBill(id: Int, tableNum: String) {
        return _billInfo.emit(billInfoRemoteDataSource.getBillInfo(id, tableNum))
    }

}


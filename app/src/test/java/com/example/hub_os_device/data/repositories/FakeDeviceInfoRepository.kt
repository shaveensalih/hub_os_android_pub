package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.domain.repositories.DeviceInfoRepository
import com.example.hub_os_device.model.Advertisement
import com.example.hub_os_device.model.Advertiser
import com.example.hub_os_device.model.DeviceInfo
import com.example.hub_os_device.model.Establishment
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeDeviceInfoRepository : DeviceInfoRepository {

    private var deviceInfo: DeviceInfo =
        DeviceInfo(
            "1",
            Advertiser(1, "Ivy", ""),
            listOf(
                Advertisement("1",
                    listOf("1a", "1b", "1c"),
                    "1s",
                    "1banner",
                    null,
                    "#000000",
                    Advertiser(0, "tenant1", null)),
                Advertisement("2",
                    listOf("2a", "2b", "2c", "2d"),
                    "2s",
                    "2banner",
                    null,
                    "#000000",
                    Advertiser(0, "tenant2", null)),
                Advertisement("3",
                    null,
                    "3s",
                    "3banner",
                    "3video",
                    "#000000",
                    Advertiser(0, "tenant1", null)),
                Advertisement("4",
                    null,
                    "4s",
                    "4banner",
                    "4video",
                    "#000000",
                    Advertiser(0, "tenant3", null))
            ),
            Establishment("", "ivy", "", "", "", "", true, "10", "5", 40, listOf()),
        )

    private var shouldReturnNetworkError: Boolean = false
    private var httpExceptionCode: Int? = null
    private var pinCode: String = "000000"

    fun setShouldReturnNetworkError(value: Boolean) {
        shouldReturnNetworkError = value
    }

    fun setHttpExceptionCode(value: Int) {
        httpExceptionCode = value
    }

    fun setPinCode(value: String) {
        pinCode = value
    }

    fun changeAds(ads: List<Advertisement>?){
        deviceInfo = deviceInfo.copy(advertisements = ads)
    }

    private val _deviceInfo = MutableSharedFlow<Result<DeviceInfo>>(replay = 1)
    private val _registerState = MutableSharedFlow<Result<DeviceInfo>>(replay = 1)
    override val deviceInfoState: SharedFlow<Result<DeviceInfo>> = _deviceInfo.asSharedFlow()
    override val registerState: SharedFlow<Result<DeviceInfo>> = _registerState.asSharedFlow()

    override suspend fun getDeviceInfo(id: Int) {
        if (shouldReturnNetworkError) {
            println("inside network error")
            _deviceInfo.emit(Result.failure(IOException("Network Exception")))
        } else if (httpExceptionCode != null)
            _deviceInfo.emit(Result.failure(HttpException(Response.error<HttpException>(httpExceptionCode!!,
                ResponseBody.create(
                    null, "{}")))))
        else
            _deviceInfo.emit(Result.success(deviceInfo))
    }

    override suspend fun registerDevice(pinCode: String) {
        if (shouldReturnNetworkError) {
            _registerState.emit(Result.failure(IOException("Network Exception")))
            _deviceInfo.emit(Result.failure(IOException("Network Exception")))
        } else if (pinCode != this.pinCode) {
            println("wrong pin")
            _registerState.emit(Result.failure(HttpException(Response.error<HttpException>(404,
                ResponseBody.create(
                    null, "{}")))))
        } else {
            _registerState.emit(Result.success(deviceInfo))
            _deviceInfo.emit(Result.success(deviceInfo))
        }
    }
}
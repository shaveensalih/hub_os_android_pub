package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.domain.repositories.BillInfoRepository
import com.example.hub_os_device.domain.repositories.WouldYouRatherRepository
import com.example.hub_os_device.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.shareIn
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeWouldYouRatherRepository(
) : WouldYouRatherRepository {

    private val wouldYouRatherResult = WouldYouRatherResult("0", "Would you rather sleep or eat?")
    private var shouldReturnNetworkError: Boolean = false

    fun setShouldReturnNetworkError(value: Boolean) {
        shouldReturnNetworkError = value
    }

    val _questionFlow: MutableSharedFlow<Result<WouldYouRatherResult>> = MutableSharedFlow(1)
    override val questionFlow: SharedFlow<Result<WouldYouRatherResult>> = _questionFlow.asSharedFlow()

    override suspend fun getQuestion() {
        if (shouldReturnNetworkError) {
            _questionFlow.emit(Result.failure(IOException("Network Exception")))
        }
        else _questionFlow.emit(Result.success(wouldYouRatherResult))
    }
}
package com.example.hub_os_device.framework

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.hub_os_device.data.local.SharedPreferencesHelper
import com.example.hub_os_device.domain.repositories.DeviceInfoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class AdsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val sharedPreferences: SharedPreferencesHelper
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO)
        {
            try {
                val id = sharedPreferences.getInt("deviceId", 0)
                deviceInfoRepository.getDeviceInfo(id)
                return@withContext Result.success()
            } catch (e: Error) {
                return@withContext Result.failure()
            }
        }
    }
}
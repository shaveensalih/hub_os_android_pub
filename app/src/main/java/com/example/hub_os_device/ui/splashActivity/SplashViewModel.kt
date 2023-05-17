package com.example.hub_os_device.ui.splashActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.data.local.SharedPreferencesHelper
import com.example.hub_os_device.domain.repositories.ConfigRepository
import com.example.hub_os_device.domain.repositories.DeviceInfoRepository
import com.example.hub_os_device.model.Config
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlin.random.Random

sealed class APKState {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }

    data class DOWNLOADING<Int>(val progress: Int) : APKState()
    object SUCCESS : APKState()
    data class FAILURE(val message: String) : APKState()
}

sealed class ConfigState {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }

    object LOADING : ConfigState()
    data class SUCCESS(val config: Config?) : ConfigState()
    data class UPDATENEEDED(val config: Config) : ConfigState()
    data class DEVICENOTREGISTERED(val message: String) : ConfigState()
    data class FAILURE(val message: String) : ConfigState()
}

sealed class RegisterState {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }

    object INCORRECTPIN : RegisterState()
    data class FAILURE(val message: String) : RegisterState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val sharedPreferences: SharedPreferencesHelper,
) :
    ViewModel() {

    private val _apkStateFlow: MutableStateFlow<APKState?> = MutableStateFlow<APKState?>(null)
    val apkState: StateFlow<APKState?> =
        _apkStateFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val config: StateFlow<ConfigState> =
        combine(
            configRepository.configState,
            deviceInfoRepository.deviceInfoState,
        )
        { conf, devInf ->
            conf.workAroundFold(
                onSuccess = { config ->
                    if (checkForUpdate(config)) return@combine ConfigState.UPDATENEEDED(config)
                    devInf.workAroundFold(
                        onSuccess = {
                            if (it.establishment == null || it.advertisements == null) {
                                return@combine ConfigState.FAILURE("Establishment not configured correctly.")
                            }
                            return@combine ConfigState.SUCCESS(config)
                        },
                        onFailure = { error ->
                            if (error is HttpException) {
                                if (error.code() == 404) {
                                    return@combine ConfigState.DEVICENOTREGISTERED(
                                        error.localizedMessage ?: "Device is not registered."
                                    )
                                }
                            }
                            return@combine ConfigState.FAILURE(
                                error.localizedMessage ?: "Unknown Error whilst getting device id."
                            )
                        }
                    )
                }, onFailure = {
                    return@combine ConfigState.FAILURE(
                        it.localizedMessage ?: "Unknown Error whilst configuring device."
                    )
                }
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ConfigState.LOADING)

    val registerState: SharedFlow<RegisterState?> = deviceInfoRepository.registerState.map {
        it.workAroundFold(onSuccess = { deviceInfo ->
            //Save registered ID and get the device info
            val id: Int = deviceInfo.id.toInt()
            sharedPreferences.putInt("deviceId", id)
            deviceInfoRepository.getDeviceInfo(id)
        }, onFailure = { error ->
            if (error is HttpException) {
                if (error.code() == 404) {
                    return@map RegisterState.INCORRECTPIN
                }
            }
            return@map RegisterState.FAILURE(error.localizedMessage
                ?: "Unknown Error whilst registering device")
        })
        return@map null
    }.shareIn(viewModelScope, SharingStarted.Eagerly)

    init {
        makeInitialCalls()
    }

    private fun checkForUpdate(config: Config?): Boolean {
        val latestVersion = config?.app?.latestVersion?.splitToSequence(".")?.toList()
        val curVersion = config?.app?.currentVersion?.splitToSequence(".")?.toList()
        latestVersion?.forEachIndexed { index, s ->
            if (curVersion != null && index < curVersion.size && s > curVersion[index]) return true
        }
        return false
    }

    fun makeInitialCalls() {
        viewModelScope.launch {
            configRepository.getConfig()
            val id = sharedPreferences.getInt("deviceId", 0)
            deviceInfoRepository.getDeviceInfo(id)
        }
    }

    fun registerDevice(pinCode: String) {
        viewModelScope.launch {
            deviceInfoRepository.registerDevice(pinCode)
        }
    }

    fun getNewApk(url: String, file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            var input: InputStream? = null
            var output: OutputStream? = null
            var connection: HttpURLConnection? = null
            try {
                val sUrl = URL(url)
                connection = sUrl.openConnection() as HttpURLConnection
                connection.connect()

                val fileLength = connection.contentLength

                // download the file
                input = connection.inputStream
                output = FileOutputStream(file)
                val data = ByteArray(1024)
                var count: Int
                var total: Long = 0
                while (input.read(data).also { count = it } != -1) {
                    // allow canceling with back button
                    if (NonCancellable.isCancelled) {
                        input.close()
                    }
                    total += count
                    output.write(data, 0, count)
                    _apkStateFlow.emit(APKState.DOWNLOADING(((total * 100) / fileLength).toInt()))

                }
            } catch (e: Exception) {
                _apkStateFlow.emit(APKState.FAILURE(e.message ?: "Unknown Error"))
                e.printStackTrace()
            } finally {
                _apkStateFlow.emit(APKState.SUCCESS)
                output?.close()
                input?.close()
                connection?.disconnect()
            }
        }
    }

}

inline fun <R, reified T> Result<T>.workAroundFold(
    onSuccess: (value: T) -> R,
    onFailure: (exception: Throwable) -> R,
): R = when {
    isSuccess -> {
        val value = getOrNull()
        try {
            onSuccess(value as T)
        } catch (e: ClassCastException) {
            // This block of code is only executed in testing environment, when we are mocking a
            // function that returns a `Result` object.
            val valueNotNull = value!!
            if ((value as Result<*>).isSuccess) {
                valueNotNull::class.java.getDeclaredField("value").let {
                    it.isAccessible = true
                    it.get(value) as T
                }.let(onSuccess)
            } else {
                valueNotNull::class.java.getDeclaredField("value").let {
                    it.isAccessible = true
                    it.get(value)
                }.let { failure ->
                    failure!!::class.java.getDeclaredField("exception").let {
                        it.isAccessible = true
                        it.get(failure) as Exception
                    }
                }.let(onFailure)
            }
        }
    }
    else -> onFailure(exceptionOrNull() ?: Exception())
}


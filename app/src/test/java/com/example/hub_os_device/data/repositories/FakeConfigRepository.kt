package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.domain.repositories.ConfigRepository
import com.example.hub_os_device.model.App
import com.example.hub_os_device.model.Config
import com.example.hub_os_device.model.Socials
import junit.runner.Version
import kotlinx.coroutines.flow.*
import java.io.IOException

class FakeConfigRepository: ConfigRepository {

    private var app: App = App("1.0.0", "app link", "000000", "1.0.0",)
    private var config: Config = Config(Socials("Insta Link","Web Link", "Number"), app)

    private val _config = MutableSharedFlow<Result<Config>>(1)
    override val configState: SharedFlow<Result<Config>> = _config.asSharedFlow()

    private var shouldReturnNetworkError: Boolean = false

    fun setShouldReturnNetworkError(value: Boolean){
        shouldReturnNetworkError = value
    }

    fun setNewVersion(value: String){
        config = config.copy(app = app.copy(latestVersion = value))
    }

    override suspend fun getConfig() {
        if(shouldReturnNetworkError) _config.emit(Result.failure(IOException("Error")))
        else {
            _config.emit(Result.success(config))
        }
    }
}
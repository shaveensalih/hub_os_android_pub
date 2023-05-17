package com.example.hub_os_device.ui.mainActivity.fragments

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.hub_os_device.domain.repositories.WeatherInfoRepository
import com.example.hub_os_device.model.WeatherSummary
import com.example.hub_os_device.ui.RequestScheduler
import com.example.hub_os_device.ui.setupDailyRequests
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random


@HiltViewModel
class WeatherWidgetViewModel @Inject constructor(
    private val weatherInfoRepository: WeatherInfoRepository,
    application: Application,
) :
    AndroidViewModel(application), RequestScheduler {

    private val weatherInfoState: StateFlow<Result<WeatherSummary>?> =
        weatherInfoRepository.weatherInfoState.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null,
        )

    private val _currentWeatherFlow: MutableSharedFlow<CurrentWeatherUIState?> = MutableSharedFlow()
    val currentWeatherFlow: SharedFlow<CurrentWeatherUIState?> =
        _currentWeatherFlow.asSharedFlow().shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val weeksWeatherFlow: SharedFlow<WeatherState<List<WeeklyWeatherUIState>>> =
        weatherInfoState.map { res ->

            res?.workAroundFold(onSuccess = {
                return@map WeatherState.SUCCESS(it.daily.map { weather ->
                    WeeklyWeatherUIState(
                        LocalDate.parse(
                            weather.timestamp,
                            DateTimeFormatter.ofPattern("uuuu-MM-dd H:mm:ss")

                        ).dayOfWeek.toString().take(3), weather.temp
                    )
                })
            }, onFailure = { error ->
                return@map WeatherState.FAILURE(error.localizedMessage ?: "Unknown Error")
            })

            return@map WeatherState.LOADING
        }.shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    init {
        getWeather(application.applicationContext)
        this.setupDailyRequests<WeatherWorker>(application, "weather", "weatherWorker")
        setupHourlyWeatherUpdates()
    }

    fun getWeather(applicationContext: Context) {
        with(viewModelScope) {
            val id: Int = applicationContext.getSharedPreferences("deviceIdPref", Context.MODE_PRIVATE)
                .getInt("deviceId", 0)

            val updateCurrentWeatherParentScope = launch {
                setupCurrentWeatherDailyUpdater()
            }

            launch(context = updateCurrentWeatherParentScope) {
                weatherInfoRepository.getWeatherInfo(id)
            }

        }
    }

    private suspend fun setupCurrentWeatherDailyUpdater() {
        weatherInfoState.collectLatest { res ->
            res?.onSuccess {
                updateCurrentWeather(it)
            }
        }
    }

    private suspend fun updateCurrentWeather(
        value: WeatherSummary?,
    ) {
        //Getting the current hours weather from the main weather stream and pushing it to the current weather flow
        val currentHoursWeather = value?.hourly?.find {
            val date = LocalTime.parse(
                it.timestamp,
                DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
            )
            val hour: Int = date.get(ChronoField.HOUR_OF_DAY)
            return@find hour == LocalTime.now().hour
        }
        _currentWeatherFlow.emit(currentHoursWeather?.let {
            CurrentWeatherUIState(
                currentHoursWeather.code,
                currentHoursWeather.temp,
                currentHoursWeather.desc
            )
        })
    }

    //Setup hourly requests to update hourly weather
    private fun setupHourlyWeatherUpdates() {
        val calendar: Calendar = Calendar.getInstance()
        val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

        scheduler.scheduleAtFixedRate(
            {
                weatherInfoState.value?.onSuccess {
                    viewModelScope.launch {
                        updateCurrentWeather(it)
                    }
                }

            },
            millisToNextHour(calendar),
            60 * 60 * 1000,
            TimeUnit.MILLISECONDS
        )
    }
}

@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherInfoRepository: WeatherInfoRepository,
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO)
        {
            try {
                val id: Int = applicationContext.getSharedPreferences("deviceIdPref", Context.MODE_PRIVATE)
                    .getInt("deviceId", 0)
                weatherInfoRepository.getWeatherInfo(id)
                return@withContext Result.success()
            } catch (e: Error) {
                println(e)
                return@withContext Result.failure()
            }
        }
    }
}

data class CurrentWeatherUIState(
    val weatherCode: Int,
    val temp: Double,
    val desc: String?,
)

data class WeeklyWeatherUIState(
    val day: String,
    val temp: Double,
)

sealed class WeatherState<out T> {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }

    object LOADING : WeatherState<Nothing>()
    data class SUCCESS<T>(val successValue: T?) : WeatherState<T>()
    data class FAILURE(val message: String) : WeatherState<Nothing>()
}


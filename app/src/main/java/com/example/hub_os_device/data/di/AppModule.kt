package com.example.hub_os_device.data.di

import android.app.Application
import com.example.hub_os_device.BuildConfig
import com.example.hub_os_device.data.local.SharedPreferencesHelper
import com.example.hub_os_device.data.local.SharedPreferencesHelperImpl
import com.example.hub_os_device.data.remote.*
import com.example.hub_os_device.data.repositories.*
import com.example.hub_os_device.domain.repositories.*
import com.example.hub_os_device.exception.ResultCallAdapterFactory
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //DISPATCHERS
    @DefaultDispatcher
    @Provides
    @Singleton
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    @Singleton
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    @Singleton
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Singleton
    fun provideSharedPreferencesHelper(application: Application): SharedPreferencesHelper {
        return SharedPreferencesHelperImpl(application)
    }

    @HubOSApi
    @Provides
    @Singleton
    fun provideBaseApi(): Retrofit {
        val httpClient =
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)

        httpClient.addInterceptor { chain ->
            val original: Request = chain.request()
            // Request customization: add request headers
            val requestBuilder: Request.Builder = original.newBuilder()
                .header("Accept", "application/json")
                .header(
                    "Authorization",
                    BuildConfig.API_KEY
                )
            val request: Request = requestBuilder.build()
            chain.proceed(request)
        }

        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl("https://hubos.backspace.tools/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .client(httpClient.build())
            .build()

    }


    @WouldYouRatherApi
    @Provides
    fun provideWouldYouRatherBaseApi(): Retrofit {
        val client = OkHttpClient.Builder()

        return Retrofit.Builder()
            .baseUrl("https://would-you-rather-api.abaanshanid.repl.co/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .client(client.build())
            .build()
    }

    @TriviaApi
    @Provides
    fun provideTriviaApi(): Retrofit {
        val client = OkHttpClient.Builder()

        return Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .client(client.build())
            .build()
    }

    @HoroscopeApi
    @Provides
    fun provideHoroscopeBaseApi(): Retrofit {
        val client = OkHttpClient.Builder()

        client.addInterceptor { chain ->
            val original: Request = chain.request()

            // Request customization: add request headers
            val requestBuilder: Request.Builder = original.newBuilder()
                .header(
                    "X-RapidAPI-Key",
                    BuildConfig.HOROSCOPE_API_KEY
                )
            val request: Request = requestBuilder.build()
            chain.proceed(request)
        }

        return Retrofit.Builder()
            .baseUrl("https://sameer-kumar-aztro-v1.p.rapidapi.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .client(client.build())
            .build()
    }

    @Singleton
    @Provides
    fun provideFirebase(): Firebase = Firebase

    @Provides
    fun provideTriviaInfoApi(@TriviaApi triviaApi: Retrofit): TriviaInfoApi {
        return triviaApi
            .create()
    }

    @Provides
    fun provideTriviaInfoRepository(
        triviaInfoApi: TriviaInfoApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): TriviaRepository {
        return TriviaRepositoryImpl(TriviaRemoteDataSource(triviaInfoApi, ioDispatcher), ioDispatcher)
    }

    @Provides
    fun provideHoroscopeApi(@HoroscopeApi horoscopeApi: Retrofit): com.example.hub_os_device.data.remote.HoroscopeApi {
        return horoscopeApi
            .create()
    }

    @Provides
    fun provideHoroscopeRepository(
        horoscopeApi: com.example.hub_os_device.data.remote.HoroscopeApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): HoroscopeRepository {
        return HoroscopeRepositoryImpl(HoroscopeRemoteDataSource(horoscopeApi, ioDispatcher))
    }

    @Provides
    @Singleton
    fun provideDeviceInfoApi(@HubOSApi baseAPI: Retrofit): DeviceInfoApi {
        return baseAPI
            .create()
    }

    @Provides
    @Singleton
    fun provideDeviceInfoRepository(
        deviceInfoApi: DeviceInfoApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): DeviceInfoRepository {
        return DeviceInfoRepositoryImpl(DeviceInfoRemoteDataSource(deviceInfoApi, ioDispatcher))
    }

    @Provides
    @Singleton
    fun provideWeatherInfoApi(@HubOSApi baseAPI: Retrofit): WeatherInfoApi {
        return baseAPI
            .create()
    }

    @Provides
    @Singleton
    fun provideWeatherInfoRepository(
        weatherInfoApi: WeatherInfoApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): WeatherInfoRepository {
        return WeatherInfoRepositoryImpl(WeatherInfoRemoteDataSource(weatherInfoApi, ioDispatcher))
    }

    @Provides
    fun provideWouldYouRatherApi(@WouldYouRatherApi wouldYouRatherApi: Retrofit): com.example.hub_os_device.data.remote.WouldYouRatherApi {
        return wouldYouRatherApi
            .create()
    }

    @Provides
    fun provideWouldYouRatherRepository(
        wouldYouRatherApi: com.example.hub_os_device.data.remote.WouldYouRatherApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): WouldYouRatherRepository {
        return WouldYouRatherRepositoryImpl(WouldYouRatherRemoteDataSource(wouldYouRatherApi, ioDispatcher))
    }

    @Provides
    @Singleton
    fun provideConfig(@HubOSApi baseAPI: Retrofit): ConfigApi {
        return baseAPI
            .create()
    }

    @Provides
    @Singleton
    fun provideConfigRepository(
        configApi: ConfigApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): ConfigRepository {
        return ConfigRepositoryImpl(ConfigRemoteDataSource(configApi, ioDispatcher))
    }

    @Provides
    @Singleton
    fun provideBillInfoApi(@HubOSApi baseAPI: Retrofit): BillInfoApi {
        return baseAPI
            .create()
    }

    @Provides
    fun provideBillInfoRepository(
        billInfo: BillInfoApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): BillInfoRepository {
        return BillInfoRepositoryImpl(BillInfoRemoteDataSource(billInfo, ioDispatcher))
    }

    @Provides
    @Singleton
    fun provideRestaurantInfoApi(@HubOSApi baseAPI: Retrofit): RestaurantInfoApi {
        return baseAPI
            .create()
    }

    @Provides
    @Singleton
    fun provideRestaurantRepository(
        restaurantInfoApi: RestaurantInfoApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): RestaurantInfoRepository {
        return RestaurantInfoRepositoryImpl(RestaurantInfoRemoteDataSource(restaurantInfoApi, ioDispatcher))
    }

    @Provides
    @Singleton
    fun provideTopicOfDayApi(@HubOSApi baseAPI: Retrofit): TopicOfDayApi {
        return baseAPI
            .create()
    }

    @Provides
    @Singleton
    fun provideTopicOfDayRepository(
        topicOfDayApi: TopicOfDayApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): TopicOfDayRepository {
        return TopicOfDayRepositoryImpl(TopicOfDayRemoteDataSource(topicOfDayApi, ioDispatcher))
    }
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HubOSApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WouldYouRatherApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TriviaApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HoroscopeApi

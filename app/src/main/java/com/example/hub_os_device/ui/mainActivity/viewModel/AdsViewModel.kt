package com.example.hub_os_device.ui.mainActivity.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.domain.repositories.DeviceInfoRepository
import com.example.hub_os_device.model.Advertisement
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import com.google.android.exoplayer2.offline.DownloadRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AdsViewModel @Inject constructor(private val deviceInfoRepository: DeviceInfoRepository) :
    ViewModel() {

    companion object private var scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    val adsState: StateFlow<AdvertisementListUIState> =
        deviceInfoRepository.deviceInfoState.map {
            it.workAroundFold(onSuccess = { deviceInfo ->
                if (deviceInfo.advertisements?.isEmpty() != false) return@workAroundFold
                val ads = deviceInfo.advertisements.shuffled()
                val highestPrimaryImgCount = getHighestAdPrimaryImagesCount(ads)
                val flattenedAdsList =
                    MutableList<AdvertisementUIState?>(highestPrimaryImgCount * ads.size) { null }
                populateFlattenedAdsList(ads, highestPrimaryImgCount, flattenedAdsList)
                _currentAdIndexState.emit(0)
                return@map AdvertisementListUIState(ads = flattenedAdsList.filterNotNull())
            }, onFailure = { error ->
                //TODO: Check if debug mode is on and if so show Error message at the bottom of current ads
                println("Error: $error")
                return@map AdvertisementListUIState(listOf())
            })
            AdvertisementListUIState(listOf())
        }.stateIn(viewModelScope, SharingStarted.Eagerly, AdvertisementListUIState(listOf()))

    private fun populateFlattenedAdsList(
        ads: List<Advertisement>, highestPrimaryImgCount: Int,
        flattenedAdsList: MutableList<AdvertisementUIState?>,
    ) {
        ads.forEachIndexed { adIndex, advertisement ->
            //Make advertisement UIState objects
            val advertisementUIState = AdvertisementUIState(
                advertisement.id.toInt(), null, advertisement.secondaryImgUrl, advertisement.bannerImgUrl,
                null, advertisement.color, null, advertisement.advertiser)

            val video: AdvertisementUIState? = downloadVideoIfPresent(advertisement, advertisementUIState)

            val primaryImgList: List<AdvertisementUIState>? = advertisement.primaryImgUrls?.map {
                advertisementUIState.copy(primaryImgUrl = it)
            }

            //Flatten the list of ads if it has images or videos by looping through them and finding their appropriate positions
            for (index in 0 until highestPrimaryImgCount)
                flattenedAdsList[(index * ads.size) + adIndex] =
                    video ?: primaryImgList!![index % primaryImgList.size]
        }
    }

    private fun getHighestAdPrimaryImagesCount(ads: List<Advertisement>): Int {
        var highestPrimaryImgCount = 1
        ads.forEach {
            if (it.primaryImgUrls != null && it.primaryImgUrls.size > highestPrimaryImgCount) {
                highestPrimaryImgCount = it.primaryImgUrls.size
            }
        }
        return highestPrimaryImgCount
    }

    private fun downloadVideoIfPresent(advertisement: Advertisement, adState: AdvertisementUIState)
            : AdvertisementUIState? {
        return advertisement.videoUrl?.let {
            downloadVideo(advertisement, adState)
            adState.copy(videoUrl = it)
        }
    }

    private fun downloadVideo(advertisement: Advertisement, advertisementUIState: AdvertisementUIState) {
        val url = Uri.parse(advertisement.videoUrl)
        val downloadRequest =
            DownloadRequest.Builder(advertisement.id, url)
                .build()
        advertisementUIState.videoUrl = advertisement.videoUrl
        advertisementUIState.downloadRequest = downloadRequest
    }

    val dineOSIntegrationFlow: StateFlow<Boolean> = deviceInfoRepository.deviceInfoState.map { result ->
        result.workAroundFold(onSuccess = {
            return@map it.establishment?.dineosIntegration ?: false
        }, onFailure = {
            return@map false
        })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _currentAdIndexState = MutableStateFlow(0)
    val currentAdIndexState: StateFlow<Int> = _currentAdIndexState.asStateFlow()

    val currentAdState: SharedFlow<AdvertisementUIState?> =
        currentAdIndexState.map { return@map getCurrentAd(it) }
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    fun stopTimer() {
        scheduler.shutdownNow()
    }

    fun startTimer() {
        scheduler = Executors.newSingleThreadScheduledExecutor()
        setTimer(0)
    }

    private fun setTimer(delay: Long) {
        scheduler.scheduleAtFixedRate(
            {
                _currentAdIndexState.update {
                    currentAdIndexState.value + 1
                }
            },
            delay,
            8000,
            TimeUnit.MILLISECONDS
        )
    }

    fun getCurrentAd(index: Int): AdvertisementUIState? {
        val ads = adsState.value.ads
        if (ads == null || ads.isEmpty()) return null
        return ads[index % ads.size]
    }
}
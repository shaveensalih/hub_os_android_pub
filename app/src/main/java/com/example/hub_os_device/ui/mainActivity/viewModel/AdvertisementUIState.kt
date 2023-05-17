package com.example.hub_os_device.ui.mainActivity.viewModel

import com.example.hub_os_device.model.Advertiser
import com.google.android.exoplayer2.offline.DownloadRequest

data class AdvertisementListUIState(val ads: List<AdvertisementUIState>)

data class AdvertisementUIState(
    val id: Int,
    var primaryImgUrl: String?,
    val secondaryImgUrl: String?,
    val bannerImgUrl: String?,
    var videoUrl: String?,
    val color: String,
    var downloadRequest: DownloadRequest?,
    val advertiser: Advertiser,
)
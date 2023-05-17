package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import collectFlowValue
import com.example.hub_os_device.MainCoroutineRule
import com.example.hub_os_device.data.repositories.FakeDeviceInfoRepository
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AdsViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: AdsViewModel
    private var deviceInfoRepository: FakeDeviceInfoRepository = FakeDeviceInfoRepository()

    @Before
    fun setup() {
        viewModel = AdsViewModel(deviceInfoRepository)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get advertisementUIState list successfully`() = runTest {
        deviceInfoRepository.getDeviceInfo(0)
        val res = collectFlowValue(viewModel.adsState)?.ads
        Truth.assertThat(res?.first()).isInstanceOf(AdvertisementUIState::class.java)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get empty list if ads are null`() = runTest {
        deviceInfoRepository.changeAds(null)
        deviceInfoRepository.getDeviceInfo(0)
        val res = collectFlowValue(viewModel.adsState)?.ads
        Truth.assertThat(res).isEmpty()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get empty list if error occurs`() = runTest {
        deviceInfoRepository.setShouldReturnNetworkError(true)
        deviceInfoRepository.getDeviceInfo(0)
        val res = collectFlowValue(viewModel.adsState)?.ads
        Truth.assertThat(res).isEmpty()
    }

}
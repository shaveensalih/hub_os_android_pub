package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import collectFlowValue
import com.example.hub_os_device.MainCoroutineRule
import com.example.hub_os_device.data.local.FakeSharedPreferencesHelperImpl
import com.example.hub_os_device.data.repositories.FakeDeviceInfoRepository
import com.example.hub_os_device.ui.models.UIResult
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.BeforeEach

internal class QRCodeFragmentViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: QRCodeFragmentViewModel
    private val deviceInfoRepository: FakeDeviceInfoRepository = FakeDeviceInfoRepository()

    @Before
    fun setup() {
        viewModel = QRCodeFragmentViewModel(deviceInfoRepository)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get qrCode value on success`() = runTest {
        deviceInfoRepository.getDeviceInfo(0)
        Truth.assertThat(collectFlowValue(viewModel.qrCodeInfoFlow))
            .isInstanceOf(QRCodeWidgetInfoUIState::class.java)
    }
}
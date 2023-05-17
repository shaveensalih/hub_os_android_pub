package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import collectFlowValue
import com.example.hub_os_device.MainCoroutineRule
import com.example.hub_os_device.data.local.FakeSharedPreferencesHelperImpl
import com.example.hub_os_device.data.repositories.FakeDeviceInfoRepository
import com.example.hub_os_device.data.repositories.FakeRestaurantInfoRepository
import com.example.hub_os_device.ui.models.UIResult
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class MenuFragmentViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: MenuFragmentViewModel
    private val restaurantInfoRepository: FakeRestaurantInfoRepository = FakeRestaurantInfoRepository()
    private val deviceInfoRepository: FakeDeviceInfoRepository = FakeDeviceInfoRepository()
    private val fakeSharedPreferencesHelperImpl: FakeSharedPreferencesHelperImpl =
        FakeSharedPreferencesHelperImpl()

    @Before
    fun setup() {
        viewModel = MenuFragmentViewModel(deviceInfoRepository,
            restaurantInfoRepository,
            fakeSharedPreferencesHelperImpl)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get success menu`() = runTest {
        deviceInfoRepository.getDeviceInfo(0)
        restaurantInfoRepository.getMenu(0)
        Truth.assertThat(collectFlowValue(viewModel.menuInfoFlow))
            .isInstanceOf(UIResult.SUCCESS::class.java)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle no DineOS integration failure`() = runTest {
        restaurantInfoRepository.setHttpExceptionCode(400)
        deviceInfoRepository.getDeviceInfo(0)
        restaurantInfoRepository.getMenu(0)
        Truth.assertThat(collectFlowValue(viewModel.menuInfoFlow))
            .isEqualTo(UIResult.FAILURE("Oops... No DineOS integration."))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle no restaurant connection failure`() = runTest {
        restaurantInfoRepository.setHttpExceptionCode(404)
        deviceInfoRepository.getDeviceInfo(0)
        restaurantInfoRepository.getMenu(0)
        Truth.assertThat(collectFlowValue(viewModel.menuInfoFlow))
            .isEqualTo(UIResult.FAILURE("Oops... Not connected to any restaurant."))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle bad request failure`() = runTest {
        restaurantInfoRepository.setHttpExceptionCode(422)
        deviceInfoRepository.getDeviceInfo(0)
        restaurantInfoRepository.getMenu(0)
        Truth.assertThat(collectFlowValue(viewModel.menuInfoFlow))
            .isEqualTo(UIResult.FAILURE("Oops... Could not process menu request."))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle null menu`() = runTest {
        restaurantInfoRepository.setMenu(null)
        deviceInfoRepository.getDeviceInfo(0)
        restaurantInfoRepository.getMenu(0)
        Truth.assertThat(collectFlowValue(viewModel.menuInfoFlow))
            .isEqualTo(UIResult.FAILURE("Oops... No menu found."))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle menu network error`() = runTest {
        restaurantInfoRepository.setShouldReturnNetworkError(true)
        deviceInfoRepository.getDeviceInfo(0)
        restaurantInfoRepository.getMenu(0)
        Truth.assertThat(collectFlowValue(viewModel.menuInfoFlow))
            .isInstanceOf(UIResult.FAILURE::class.java)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle device info network error`() = runTest {
        restaurantInfoRepository.setShouldReturnNetworkError(true)
        deviceInfoRepository.getDeviceInfo(0)
        Truth.assertThat(collectFlowValue(viewModel.menuInfoFlow))
            .isInstanceOf(UIResult.FAILURE::class.java)
    }
}
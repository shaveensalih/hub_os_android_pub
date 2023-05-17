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
import org.junit.jupiter.api.Assertions.*

internal class SelfServiceViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: SelfServiceViewModel
    private val restaurantInfoRepository: FakeRestaurantInfoRepository = FakeRestaurantInfoRepository()
    private val deviceInfoRepository: FakeDeviceInfoRepository = FakeDeviceInfoRepository()
    private val fakeSharedPreferencesHelperImpl: FakeSharedPreferencesHelperImpl =
        FakeSharedPreferencesHelperImpl()

    @Before
    fun setup() {
        viewModel = SelfServiceViewModel(deviceInfoRepository,
            restaurantInfoRepository,
            fakeSharedPreferencesHelperImpl)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get menu successfully`() = runTest {
        deviceInfoRepository.getDeviceInfo(0)
        restaurantInfoRepository.getMenu(0)
        Truth.assertThat(collectFlowValue(viewModel.serviceableItemsFlow))
            .isInstanceOf(UIResult.SUCCESS::class.java)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle no DineOS integration failure for menu`() = runTest {
        restaurantInfoRepository.setHttpExceptionCode(400)
        deviceInfoRepository.getDeviceInfo(0)
        restaurantInfoRepository.getMenu(0)
        Truth.assertThat(collectFlowValue(viewModel.serviceableItemsFlow))
            .isEqualTo(UIResult.FAILURE("Oops... No DineOS integration."))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle no restaurant connection failure for menu`() = runTest {
        restaurantInfoRepository.setHttpExceptionCode(404)
        deviceInfoRepository.getDeviceInfo(0)
        restaurantInfoRepository.getMenu(0)
        Truth.assertThat(collectFlowValue(viewModel.serviceableItemsFlow))
            .isEqualTo(UIResult.FAILURE("Oops... Not connected to any restaurant."))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle bad request failure for menu`() = runTest {
        restaurantInfoRepository.setHttpExceptionCode(422)
        deviceInfoRepository.getDeviceInfo(0)
        restaurantInfoRepository.getMenu(0)
        Truth.assertThat(collectFlowValue(viewModel.serviceableItemsFlow))
            .isEqualTo(UIResult.FAILURE("Oops... Could not process menu request."))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle null menu`() = runTest {
        restaurantInfoRepository.setMenu(null)
        deviceInfoRepository.getDeviceInfo(0)
        restaurantInfoRepository.getMenu(0)
        Truth.assertThat(collectFlowValue(viewModel.serviceableItemsFlow))
            .isEqualTo(UIResult.FAILURE("Oops... No menu found."))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle menu network error`() = runTest {
        restaurantInfoRepository.setShouldReturnNetworkError(true)
        deviceInfoRepository.getDeviceInfo(0)
        restaurantInfoRepository.getMenu(0)
        Truth.assertThat(collectFlowValue(viewModel.serviceableItemsFlow))
            .isInstanceOf(UIResult.FAILURE::class.java)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle device info network error for menu`() = runTest {
        restaurantInfoRepository.setShouldReturnNetworkError(true)
        deviceInfoRepository.getDeviceInfo(0)
        Truth.assertThat(collectFlowValue(viewModel.serviceableItemsFlow))
            .isInstanceOf(UIResult.FAILURE::class.java)

    }@ExperimentalCoroutinesApi
    @Test
    fun `order self service item successfully`() = runTest {
        viewModel.orderItem("0")
        Truth.assertThat(collectFlowValue(viewModel.selfServiceResultFlow))
            .isInstanceOf(UIResult.SUCCESS::class.java)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle no DineOS integration for self service failure`() = runTest {
        restaurantInfoRepository.setHttpExceptionCode(400)
        viewModel.orderItem("0")
        Truth.assertThat(collectFlowValue(viewModel.selfServiceResultFlow))
            .isEqualTo(UIResult.FAILURE("No DineOS integration."))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle no restaurant connection for self service failure`() = runTest {
        restaurantInfoRepository.setHttpExceptionCode(404)
        viewModel.orderItem("0")
        Truth.assertThat(collectFlowValue(viewModel.selfServiceResultFlow))
            .isEqualTo(UIResult.FAILURE("Could not order this menu item."))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle bad request for self service failure`() = runTest {
        restaurantInfoRepository.setHttpExceptionCode(422)
        viewModel.orderItem("0")
        Truth.assertThat(collectFlowValue(viewModel.selfServiceResultFlow))
            .isEqualTo(UIResult.FAILURE("Could not process order request"))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `handle self service network error`() = runTest {
        restaurantInfoRepository.setShouldReturnNetworkError(true)
        viewModel.orderItem("0")
        Truth.assertThat(collectFlowValue(viewModel.selfServiceResultFlow))
            .isInstanceOf(UIResult.FAILURE::class.java)
    }
}
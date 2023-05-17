package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import collectFlowValue
import com.example.hub_os_device.MainCoroutineRule
import com.example.hub_os_device.data.local.FakeSharedPreferencesHelperImpl
import com.example.hub_os_device.data.repositories.FakeBillInfoRepository
import com.example.hub_os_device.data.repositories.FakeDeviceInfoRepository
import com.example.hub_os_device.model.BillStatus
import com.example.hub_os_device.model.Discount
import com.example.hub_os_device.model.DiscountType
import com.example.hub_os_device.ui.models.UIResult
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class SplitTheBillFragmentViewModelTest {

    val billItemUIStateList = listOf(
        BillItemUIState(0, "Pasta Chow Mein", listOf("Cheese"), "Extra sauce",
            CostSummary(11000.0, 1100.0, 440.0, 2200.0, 10340.0),
            22000.0, 4400.0, 20, 2, 0),
        BillItemUIState(2, "Burger", listOf("Bacon"), null,
            CostSummary(9000.0, 900.0, 360.0, 1800.0, 8460.0),
            9000.0, 1800.0, 20, 1, 1),
        BillItemUIState(3, "Burger", listOf("Bacon"), null,
            CostSummary(9000.0, 900.0, 360.0, 1800.0, 8460.0),
            9000.0, 1800.0, 20, 1, 0),
        BillItemUIState(4, "Bullfrog", null, null,
            CostSummary(8000.0, 800.0, 320.0, 1600.0, 7520.0),
            8000.0, 1600.0, 20, 1, 0),
    )

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: SplitTheBillFragmentViewModel
    private val deviceInfoRepository: FakeDeviceInfoRepository = FakeDeviceInfoRepository()
    private val billInfoRepository: FakeBillInfoRepository = FakeBillInfoRepository()

    @Before
    fun setup() {
        viewModel = SplitTheBillFragmentViewModel(FakeBillInfoRepository(),
            deviceInfoRepository,
            FakeSharedPreferencesHelperImpl())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get cost summary of multiple items`() = runTest {
        deviceInfoRepository.getDeviceInfo(0)
        viewModel.getBillInfo()
        collectFlowValue(viewModel.billInfoUIStateFlow)
        viewModel.changeSelectedItems(
            BillItemSelectedUIState(0, CostSummary(11000.0, 1100.0, 440.0, 2200.0, 10340.0),
                2),
        )
        collectFlowValue(viewModel.totalFlow)
        Truth.assertThat(viewModel.totalFlow.replayCache.first()).isEqualTo(CostSummary(22000.0, 2200.0, 880.0, 4400.0, 20680.0))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get cost summary of item with split`() = runTest {
        deviceInfoRepository.getDeviceInfo(0)
        viewModel.getBillInfo()
        collectFlowValue(viewModel.billInfoUIStateFlow)
        viewModel.changeSelectedItems(
            BillItemSelectedUIState(0, CostSummary(11000.0, 1100.0, 440.0, 2200.0, 10340.0),
                1, 2),
        )
        val res = collectFlowValue(viewModel.totalFlow)
        Truth.assertThat(res).isEqualTo(CostSummary(5500.0, 550.0, 220.0, 1100.0, 5170.0))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get cost summary of all items`() = runTest {
        deviceInfoRepository.getDeviceInfo(0)
        viewModel.getBillInfo()
        collectFlowValue(viewModel.billInfoUIStateFlow)
        billItemUIStateList.forEach {
            viewModel.changeSelectedItems(
                BillItemSelectedUIState(it.id, it.costSummary, it.quantity),
            )
        }
        val res = collectFlowValue(viewModel.totalFlow)
        Truth.assertThat(res).isEqualTo(CostSummary(48000.0, 4800.0, 1920.0, 9600.0, 45120.0))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get selected items`() = runTest {
        deviceInfoRepository.getDeviceInfo(0)
        viewModel.getBillInfo()
        viewModel.changeSelectedItems(
            BillItemSelectedUIState(0, CostSummary(11000.0, 1100.0, 550.0, 2200.0, 10450.0),
                2),
        )
        Truth.assertThat(collectFlowValue(viewModel.selectedItems))
            .isEqualTo(listOf(BillItemSelectedUIState(0,
                CostSummary(11000.0, 1100.0, 550.0, 2200.0, 10450.0),
                2,
                1)))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getBillInfoSuccessfully() = runTest {
        deviceInfoRepository.getDeviceInfo(0)
        viewModel.getBillInfo()
        Truth.assertThat(collectFlowValue(viewModel.billInfoUIStateFlow))
            .isInstanceOf(UIResult.SUCCESS::class.java)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `get bill info & check if items are correct`() = runTest {
        deviceInfoRepository.getDeviceInfo(0)
        viewModel.getBillInfo()
        billInfoRepository.setBillStatus(BillStatus.AWAITING_PAYMENT)
        billInfoRepository.setVat(5)
        billInfoRepository.setService(10)
        billInfoRepository.setDiscount(Discount(0, DiscountType.NORMAL, 20, null, null, null))
        billInfoRepository.resetBillInfo()
        val result: List<BillItemUIState>? = viewModel.getBillItemsUIState(billInfoRepository.billInfoValue!!)
        Truth.assertThat(result).containsExactlyElementsIn(billItemUIStateList)
    }
}
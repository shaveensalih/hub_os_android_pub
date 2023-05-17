package com.example.hub_os_device.ui.mainActivity.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hub_os_device.data.local.SharedPreferencesHelper
import com.example.hub_os_device.domain.repositories.BillInfoRepository
import com.example.hub_os_device.domain.repositories.DeviceInfoRepository
import com.example.hub_os_device.model.*
import com.example.hub_os_device.ui.models.UIResult
import com.example.hub_os_device.ui.splashActivity.workAroundFold
import com.example.hub_os_device.util.parseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SplitTheBillFragmentViewModel @Inject constructor(
    private val billInfoRepository: BillInfoRepository,
    deviceInfoRepository: DeviceInfoRepository,
    private val sharedPreferences: SharedPreferencesHelper,
) :
    ViewModel() {

    init {
        getBillInfo()
    }

    data class IdenticalItemGroup(
        val id: String,
        val extras: List<String>?,
        val notes: String?,
        val costSummary: CostSummary,
        val discountedPrice: Double?,
        val discountedPercentage: Int?,
        val gifted: Int,
    )

    private val _totalFlow: MutableStateFlow<CostSummary> =
        MutableStateFlow(CostSummary(0.0, 0.0, null, null, 0.0))
    val totalFlow: StateFlow<CostSummary> = _totalFlow.asStateFlow()

    private val _billInfoFlow: SharedFlow<Result<BillInfo?>?> =
        billInfoRepository.billInfoFlow.combine(deviceInfoRepository.deviceInfoState)
        { billInfo, deviceInfo ->
            billInfo.workAroundFold(onSuccess = { billInfoSuccess ->
                deviceInfo.workAroundFold(onSuccess = {
                    billInfoSuccess?.service = it.establishment?.servicePercentage?.toDouble()?.toInt() ?: 0
                    billInfoSuccess?.vat = it.establishment?.vatPercentage?.toDouble()?.toInt()
                }, {})
                if (billInfoSuccess?.service == null) billInfoSuccess?.service = 0
                return@combine billInfo
            }, {})
            return@combine billInfo
        }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val billInfoUIStateFlow: SharedFlow<UIResult> =
        _billInfoFlow.map {
            it?.workAroundFold(onSuccess = { billInfo ->
                billInfo?.let {
                    if (billIsAwaitingPayment(it)) {
                        _totalFlow.emit(CostSummary(0.0, 0.0, 0.0, 0.0, 0.0))
                    }
                }
                val groupedItemsList = groupIdenticalItems(billInfo)
                val billList = mapToBillItemUIState(groupedItemsList)

                return@map UIResult.SUCCESS<List<BillItemUIState>>(billList)
            }, onFailure = { error -> return@map handleExceptions(error) })
            return@map UIResult.LOADING
        }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private fun mapToBillItemUIState(groupedItemsList: Map<IdenticalItemGroup, List<BillListItem>>?) =
        groupedItemsList?.map {
            val billItem: BillListItem = it.value.first()
            BillItemUIState(
                billItem.id,
                billItem.menuItem.name,
                it.key.extras,
                it.key.notes,
                costSummary = it.key.costSummary,
                price = billItem.subtotal * it.value.size,
                discountedPrice = it.key.discountedPrice?.times(it.value.size),
                quantity = it.value.size,
                discountedPercentage = it.key.discountedPercentage,
                gifted = it.key.gifted
            )
        }

    //This function groups together identical items from the bill, with their associated metadata in order to prevent repeated items in the list
    private fun groupIdenticalItems(billInfo: BillInfo?) =
        billInfo?.items?.groupBy {
            val discountedPrice =
                billInfo.discount?.let { discount -> calculateDiscount(discount, it.subtotal, it) }
            //Grouping all the same items together to calculate quantity
            IdenticalItemGroup(
                id = it.menuItem.id,
                extras = it.extras?.map { it.name }?.toList(),
                notes = it.note,
                costSummary = calcBillItemCostSummary(it, billInfo),
                discountedPrice = discountedPrice,
                discountedPercentage = if (discountedPrice != 0.0) billInfo.discount?.percentage else null,
                gifted = it.gifted
            )
        }

    private fun handleExceptions(error: Throwable): UIResult.FAILURE {
        if (error is HttpException)
            when (error.code()) {
                404 -> return UIResult.FAILURE("Oops... No active order exists on this table.\nTime to order some delicious meals!")
                400 -> return UIResult.FAILURE("Oops... No DineOS integration.")
                422 -> return UIResult.FAILURE("Oops... Could not process order request.\n${parseError(error.response()!!).message}")
            }

        return UIResult.FAILURE("Oops...${error.localizedMessage}")
    }


    private val _selectedItems =
        MutableStateFlow(listOf<BillItemSelectedUIState>())
    val selectedItems: StateFlow<List<BillItemSelectedUIState>> =
        _selectedItems.asStateFlow()

    //Calculates the cost summary of each bill item - Discount must be removed first, then vat & service is added.
    //Discount, service & vat must always be calculated with the total, NOT the subtotal.
    private fun calcBillItemCostSummary(checkedListItem: BillListItem, billInfo: BillInfo): CostSummary {

        val subTotal: Double = checkedListItem.subtotal
        var total = subTotal
        val service = calcService(subTotal, billInfo.service!!)
        val discount = calcDiscountIfExists(billInfo, subTotal, checkedListItem)
        total -= discount ?: 0.0
        val vat: Double? = calcVatIfExists(billInfo, total)
        total += (vat ?: 0.0) + service

        return CostSummary(subTotal, service, vat, discount, total)
    }

    private fun billIsAwaitingPayment(billInfo: BillInfo?): Boolean =
        billInfo?.status == BillStatus.AWAITING_PAYMENT

    private fun calcVatIfExists(billInfo: BillInfo, total: Double) =
        if (billInfo.hasVat) total * (billInfo.vat!!.toDouble() / 100.0) else null

    private fun calcService(total: Double, service: Int): Double = total * (service.toDouble().div(100))

    private fun calcDiscountIfExists(billInfo: BillInfo, total: Double, checkedListItem: BillListItem) =
        if (billInfo.hasDiscount) calculateDiscount(billInfo.discount!!, total, checkedListItem) else null

    private fun calculateDiscount(discount: Discount, total: Double, item: BillListItem): Double {
        return when (discount.type) {
            DiscountType.NORMAL -> total * (discount.percentage.toDouble() / 100.0)
            DiscountType.TIME -> addDiscountIfApplies(item, discount, hasTimeDiscount)
            DiscountType.CATEGORY -> addDiscountIfApplies(item, discount, hasCategoryDiscount)
            DiscountType.TIME_CATEGORY -> addDiscountIfApplies(item, discount, hasTimeAndCategoryDiscount)
        }
    }

    private fun addDiscountIfApplies(
        item: BillListItem, discount: Discount, checkIfDiscountApplies: (BillListItem, Discount) -> Boolean,
    ): Double = if (checkIfDiscountApplies.invoke(item, discount))
        ((item.subtotal) * (discount.percentage / 100.0)) else 0.0

    //Checks if item is ordered between the time of which the discount takes place.
    private val hasTimeDiscount = { curItem: BillListItem, discount: Discount ->
        val orderedTime = LocalDateTime.parse(curItem.timeOrdered.removeSuffix(".000000Z")).toLocalTime()
        val startTime = LocalTime.parse(discount.startingTime?.removeSuffix(".000000Z"))
        val endTime = LocalTime.parse(discount.endingTime?.removeSuffix(".000000Z"))
        orderedTime.isAfter(startTime) && orderedTime.isBefore(endTime)
    }

    //Checks if the item is within the category of which the discount takes place.
    private val hasCategoryDiscount = { curItem: BillListItem, discount: Discount ->
        discount.categoryIds!!.map { it.menuCategoryId }.contains(curItem.menuItem.categoryId)
    }

    private val hasTimeAndCategoryDiscount = { curItem: BillListItem, discount: Discount ->
        hasTimeDiscount.invoke(curItem, discount) && hasCategoryDiscount.invoke(curItem, discount)
    }

    //Function called from the UI in order to update the selected items.
    fun changeSelectedItems(item: BillItemSelectedUIState) {
        //Gets the current list of selected items
        var _selectedItemsVal = _selectedItems.value.toMutableList()

        //Gets the item if it has already been selected & gets its index
        val prevItemIndex: Int = _selectedItemsVal.indexOfFirst { it.id == item.id }
        val prevItem = if (prevItemIndex != -1) _selectedItemsVal[prevItemIndex] else null

        updateItemCS(item) //Updates item cost summary for split & quantity

        viewModelScope.launch {
            updateTotalCS(item, prevItem?.costSummary) //Updates total cost summary to include the new item

            //Removes item from list if quantity is 0 or modifies selected items list to include new item
            if (item.quantitySelected == 0)
                _selectedItemsVal = _selectedItemsVal.filter { it.id != item.id }.toMutableList()
            else if (prevItem != null) _selectedItemsVal[prevItemIndex] = item
            else _selectedItemsVal.add(item)

            _selectedItems.emit(_selectedItemsVal)
        }
    }

    //Updates total cost summary. i = item, pi = previous item, t = total, cs = cost summary
    private suspend fun updateTotalCS(item: BillItemSelectedUIState, prevItemCS: CostSummary?) {
        val (tSelected, tService, tVat, tDiscount, tTotal) = totalFlow.value
        val (iSelected, iService, iVat, iDiscount, iTotal) = (item.costSummary!!)
        val (piSelected, piService, piVat, piDiscount, piTotal) = prevItemCS
            ?: CostSummary((0.0), (0.0), (0.0), (0.0), (0.0))

        val newTotCostSummary = CostSummary(calcNewCS(tSelected, piSelected, iSelected)!!,
            calcNewCS(tService, piService, iService)!!, calcNewCS(tVat, (piVat ?: 0.0), iVat),
            calcNewCS(tDiscount, (piDiscount ?: 0.0), iDiscount), calcNewCS(tTotal, piTotal, iTotal)!!)

        _totalFlow.emit(newTotCostSummary)
    }

    private fun calcNewCS(curCSAmount: Double?, prevAmount: Double, newAmount: Double?): Double? =
        if (curCSAmount != null && newAmount != null) curCSAmount - (prevAmount - newAmount) else null

    private fun updateItemCS(item: BillItemSelectedUIState) {
        fun calcSplitAndQuantity(amount: Double?): Double? =
            amount?.div(item.splitBetween)?.times(item.quantitySelected)

        val (ogSelected, ogService, ogVat, ogDiscount, ogTotal) = item.originalCs
        item.costSummary = CostSummary(calcSplitAndQuantity(ogSelected)!!, calcSplitAndQuantity(ogService)!!,
            calcSplitAndQuantity(ogVat), calcSplitAndQuantity(ogDiscount), calcSplitAndQuantity(ogTotal)!!)
    }

    fun getBillInfo() {
        viewModelScope.launch()
        {
            val id: Int = sharedPreferences.getInt("deviceId", 0)
            val tableNum: String = sharedPreferences.getString("table_num_shared_preferences", "-1")
            billInfoRepository.getBill(id, tableNum)
        }
    }

    fun getBillItemsUIState(billInfo: BillInfo): List<BillItemUIState>? {
        val groupedItemsList = groupIdenticalItems(billInfo)
        return mapToBillItemUIState(groupedItemsList)
    }
}

data class BillItemUIState
    (
    val id: Int,
    val name: String,
    val extras: List<String>?,
    val notes: String?,
    val costSummary: CostSummary,
    val price: Double,
    val discountedPrice: Double?,
    val discountedPercentage: Int?,
    val quantity: Int,
    val gifted: Int,
)

//OriginalCs is cost summary of item without split or quantity.
data class BillItemSelectedUIState
    (
    val id: Int,
    val originalCs: CostSummary,
    var quantitySelected: Int = 0,
    var splitBetween: Int = 1,
) {
    var costSummary: CostSummary? = null
}

data class CostSummary
    (
    val selected: Double,
    val service: Double,
    val vat: Double?,
    val discount: Double?,
    val total: Double,
)

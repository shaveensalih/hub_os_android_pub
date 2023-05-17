package com.example.hub_os_device.data.repositories

import com.example.hub_os_device.domain.repositories.BillInfoRepository
import com.example.hub_os_device.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeBillInfoRepository(
) : BillInfoRepository {

    private
    var shouldReturnNetworkError: Boolean = false

    private
    var httpExceptionCode: Int? = null

    fun setShouldReturnNetworkError(value: Boolean) {
        shouldReturnNetworkError = value
    }

    fun setHttpExceptionCode(value: Int) {
        httpExceptionCode = value
    }

    fun setBill(value: BillInfo?) {
        _billInfoValue = value
    }

    fun setDiscount(value: Discount?) {
        discount = value
    }

    fun setBillStatus(value: BillStatus) {
        billStatus = value
    }

    fun setService(value: Int) {
        service = value
    }

    fun setVat(value: Int) {
        vat = value
    }

    fun resetBillInfo() {
        _billInfoValue =
            _billInfoValue?.copy(service = service, vat = vat, discount = discount, status = billStatus)
    }

    val billInfoValue: BillInfo?
        get() = _billInfoValue

    private val _billInfo: MutableSharedFlow<Result<BillInfo?>> = MutableSharedFlow<Result<BillInfo?>>(1)
    override val billInfoFlow: SharedFlow<Result<BillInfo?>> = _billInfo.asSharedFlow()

    override suspend fun getBill(id: Int, tableNumber: String) {
        if (shouldReturnNetworkError) {
            _billInfo.emit(Result.failure(IOException("Network Exception")))
        } else if (httpExceptionCode != null)
            _billInfo.emit(Result.failure(HttpException(Response.error<HttpException>(
                httpExceptionCode!!,
                ResponseBody.create(
                    null, "{}")))))
        else _billInfo.emit(Result.success(_billInfoValue))
    }

    private var billStatus = BillStatus.AWAITING_PAYMENT
    private var discount: Discount? = Discount(0, DiscountType.NORMAL, 20, null, null, null)
    private var service: Int? = null
    private var vat: Int? = null

    private var _billInfoValue: BillInfo? =
        BillInfo(
            0,
            billStatus,
            vat,
            service,
            discount,
            listOf(
                BillListItem(
                    0, 11000.0, "Extra sauce", 0, "2023-04-17T11:45:08.000000Z",
                    listOf(
                        OptionItem(0, "Cheese", 1000.0)),
                    MenuItem(
                        "1",
                        "Pasta Chow Mein",
                        "jwhkwehf kwefkajwenfkjaef kajwefkjaenf",
                        1000.0,
                        null,
                        null,
                        1,
                        1,
                        null
                    ),
                ),
                BillListItem(
                    1, 11000.0, "Extra sauce", 0, "2023-04-17T11:45:08.000000Z",
                    listOf(
                        OptionItem(0, "Cheese", 1000.0)),
                    MenuItem(
                        "1",
                        "Pasta Chow Mein",
                        "jwhkwehf kwefkajwenfkjaef kajwefkjaenf",
                        10000.0,
                        null,
                        null,
                        1,
                        1,
                        null
                    ),
                ),
                BillListItem(
                    2, 9000.0, null, 1, "2023-04-17T11:45:08.000000Z",
                    listOf(
                        OptionItem(0, "Bacon", 2000.0)),
                    MenuItem(
                        "2",
                        "Burger",
                        "jwhkwehf kwefkajwenfkjaef kajwefkjaenf",
                        7000.0,
                        null,
                        null,
                        1,
                        1,
                        null
                    ),
                ),
                BillListItem(
                    3, 9000.0, null, 0, "2023-04-17T11:45:08.000000Z",
                    listOf(
                        OptionItem(0, "Bacon", 2000.0)),
                    MenuItem(
                        "2",
                        "Burger",
                        "jwhkwehf kwefkajwenfkjaef kajwefkjaenf",
                        7000.0,
                        null,
                        null,
                        1,
                        1,
                        null
                    ),
                ),
                BillListItem(
                    4, 8000.0, null, 0, "2023-04-17T12:30:08.000000Z",
                    null,
                    MenuItem(
                        "3",
                        "Bullfrog",
                        "jwhkwehf kwefkajwenfkjaef kajwefkjaenf",
                        8000.0,
                        null,
                        null,
                        1,
                        1,
                        null
                    ),
                ),
            ),
        )
}
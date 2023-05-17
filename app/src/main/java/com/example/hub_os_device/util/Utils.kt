package com.example.hub_os_device.util

import com.example.hub_os_device.model.ErrorResult
import com.google.gson.Gson
import retrofit2.Response
import java.text.DecimalFormat

fun parseError(response: Response<*>): ErrorResult {
    return try {
        val errorJson = response.errorBody()?.string()
        val error: ErrorResult = Gson().fromJson(errorJson, ErrorResult::class.java)
        error.code = response.code()
        error
    } catch (e: Exception) {
        ErrorResult(null, "Something went wrong!")
    }
}

fun parsePrice(num: Int) : String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(num)
}
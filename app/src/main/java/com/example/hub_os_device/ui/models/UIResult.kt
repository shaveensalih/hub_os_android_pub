package com.example.hub_os_device.ui.models
import kotlin.random.Random

sealed class UIResult {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }

    object LOADING : UIResult()
    data class SUCCESS<T>(val value: T?) : UIResult()
    data class FAILURE(val message: String) : UIResult()
}
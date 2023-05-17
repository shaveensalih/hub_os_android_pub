package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DailyHoroscope(
    val color: String,
    val compatibility: String,
    val description: String,
    val mood: String,
    @SerializedName("date_range") val dateRange: String,
    @SerializedName("lucky_number") val luckyNumber: String,
    @SerializedName("lucky_time") val luckyTime: String,
    @SerializedName("current_date") val currentDate: String,
) : Serializable

enum class StarSign(val value: String) {
    @SerializedName("Aries")
    Aries("Aries"),

    @SerializedName("Taurus")
    Taurus("Taurus"),

    @SerializedName("Gemini")
    Gemini("Gemini"),

    @SerializedName("Cancer")
    Cancer("Cancer"),

    @SerializedName("Leo")
    Leo("Leo"),

    @SerializedName("Virgo")
    Virgo("Virgo"),

    @SerializedName("Libra")
    Libra("Libra"),

    @SerializedName("Scorpio")
    Scorpio("Scorpio"),

    @SerializedName("Sagittarius")
    Sagittarius("Sagittarius"),

    @SerializedName("Capricorn")
    Capricorn("Capricorn"),

    @SerializedName("Aquarius")
    Aquarius("Aquarius"),

    @SerializedName("Pisces")
    Pisces("Pisces"),
}

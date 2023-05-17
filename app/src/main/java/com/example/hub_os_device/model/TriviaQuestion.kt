package com.example.hub_os_device.model

import com.google.gson.annotations.SerializedName

data class TriviaResponse(
    val results: List<TriviaQuestion>,
    @SerializedName("response_code") val responseCode: Int,
)

data class TriviaQuestion(
    val question: String,
    val difficulty: Difficulty,
    val category: TriviaCategory,
    @SerializedName("correct_answer") val correctAnswer: String,
    @SerializedName("incorrect_answers") val incorrectAnswers: List<String>,
)

enum class TriviaCategory(val value: String) {
    @SerializedName("Art")
    Art("25"),

    @SerializedName("Computers")
    Computers("18"),

    @SerializedName("Film")
    Film("11"),

    @SerializedName("General-Knowledge")
    GeneralKnowledge("9"),

    @SerializedName("Geography")
    Geography("22"),

    @SerializedName("History")
    History("23"),

    @SerializedName("Mathematics")
    Mathematics("19"),

    @SerializedName("Music")
    Music("12"),

    @SerializedName("Sports")
    Sports("21"),

    @SerializedName("Vehicles")
    Vehicles("28"),

    @SerializedName("Video-Games")
    VideoGames("15"),

    @SerializedName("Science-Nature")
    ScienceNature("17"),
}

enum class Difficulty(val value: String) {
    @SerializedName("easy")
    EASY("easy"),

    @SerializedName("medium")
    MEDIUM("medium"),

    @SerializedName("hard")
    HARD("hard"),
}

data class TriviaToken(
    @SerializedName("response_code") val responseCode: Int,
    @SerializedName("response_message") val responseMessage: String,
    val token: String,
)

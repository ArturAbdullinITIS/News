package com.example.news.domain.entity

data class Settings(
    val language: Language,
    val interval: Interval,
    val notificationsEnabled: Boolean,
    val wifiOnly: Boolean
)


enum class Language {
    ENGLISH,
    RUSSIAN,
    FRENCH,
    GERMAN
}

enum class Interval(val minutes: Int) {
    MIN_15(15),
    MIN_30(30),
    HOUR_1(60),
    HOURS_2(120),
    HOURS_4(240),
    HOURS_8(480),
    HOUR_24(1440)
}
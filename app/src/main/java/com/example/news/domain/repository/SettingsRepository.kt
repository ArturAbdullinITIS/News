package com.example.news.domain.repository

import com.example.news.domain.entity.Language
import kotlinx.coroutines.flow.Flow
import okhttp3.internal.http2.Settings

interface SettingsRepository {
    fun getSettings(): Flow<Settings>
    suspend fun updateLanguage(language: Language)
    suspend fun updateInterval(minutes: Int)
    suspend fun updateNotificationStatus(enabled: Boolean)
    suspend fun updateWifiOnly(enabled: Boolean)
}
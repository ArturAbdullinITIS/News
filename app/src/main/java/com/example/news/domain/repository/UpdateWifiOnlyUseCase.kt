package com.example.news.domain.repository

import com.example.news.domain.entity.Interval
import com.example.news.domain.repository.SettingsRepository
import java.time.chrono.MinguoEra
import javax.inject.Inject

class UpdateWifiOnlyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.updateWifiOnly(enabled)
    }
}
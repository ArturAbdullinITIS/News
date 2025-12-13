package com.example.news.data.mapper

import androidx.compose.ui.geometry.Rect
import com.example.news.domain.entity.RefreshConfig
import com.example.news.domain.entity.Settings


fun Settings.toRefreshConfig(): RefreshConfig {
    return RefreshConfig(
        language = this.language,
        interval = this.interval,
        wifiOnly = this.wifiOnly
    )
}
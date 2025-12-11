package com.example.news.data.mapper

import androidx.compose.ui.platform.InterceptPlatformTextInput
import com.example.news.data.local.ArticlesDbModel
import com.example.news.data.remote.NewsResponseDto
import com.example.news.domain.entity.Article
import com.example.news.domain.entity.Interval
import java.text.SimpleDateFormat
import java.util.Locale


fun NewsResponseDto.toDbModels(topic: String): List<ArticlesDbModel> {
    return articles.map { dto ->
        ArticlesDbModel(
            title = dto.title,
            description = dto.description,
            url = dto.url,
            imageUrl = dto.urlToImage,
            sourceName = dto.source.name,
            topic = topic,
            publishedAt = dto.publishedAt.toTimestamp()
        )
    }
}

fun Int.toInterval(): Interval {
    return Interval.entries.first { it.minutes == this}
}


private fun String.toTimestamp(): Long {
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    return dateFormatter.parse(this)?.time ?: System.currentTimeMillis()
}

fun List<ArticlesDbModel>.toEntities(): List<Article> {
    return map {
        Article(
            title = it.title,
            description = it.description,
            imageUrl = it.imageUrl,
            sourceName = it.sourceName,
            publishedAt = it.publishedAt,
            url = it.url
        )
    }.distinct()
}
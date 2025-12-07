package com.example.news.domain.entity

data class Article(
    val title: String,
    val description: String,
    val sourceName: String,
    val publishedAt: Long,
    val url: String,
    val imageUrl: String?
)

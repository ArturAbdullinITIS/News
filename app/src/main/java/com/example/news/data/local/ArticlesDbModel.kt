package com.example.news.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


@Entity(
    tableName = "articles",
    primaryKeys = ["url", "topic"],
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionDbModel::class,
            parentColumns = ["topic"],
            childColumns = ["topic"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topic")]


)
data class ArticlesDbModel(
    val title: String,
    val description: String,
    val sourceName: String,
    val publishedAt: Long,
    val url: String,
    val imageUrl: String?,
    val topic: String
)
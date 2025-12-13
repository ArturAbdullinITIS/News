package com.example.news.data.repository

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.news.data.background.RefreshDataWorker
import com.example.news.data.local.ArticlesDbModel
import com.example.news.data.local.NewsDao
import com.example.news.data.local.SubscriptionDbModel
import com.example.news.data.mapper.toDbModels
import com.example.news.data.mapper.toEntities
import com.example.news.data.remote.NewsApiService
import com.example.news.domain.entity.Article
import com.example.news.domain.entity.RefreshConfig
import com.example.news.domain.repository.NewsRepository
import com.example.news.domain.repository.SettingsRepository
import com.example.news.presentation.screen.subscriptions.SubscriptionsCommand
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val newsDao: NewsDao,
    private val newsApiService: NewsApiService,
    private val workManager: WorkManager
): NewsRepository {
    override fun getAllSubscriptions(): Flow<List<String>> {
        return newsDao.getAllSubscriptions().map { subscriptions ->
            subscriptions.map { it.topic }
        }
    }

    override suspend fun addSubscription(topic: String) {
        newsDao.addSubscription(SubscriptionDbModel(topic))
    }

    override suspend fun updateArticlesForTopic(topic: String): Boolean {
        val articles = loadArticles(topic)
        val ids = newsDao.addArticles(articles)
        return ids.any {it != -1L}
    }


    override fun startBackgroundRefresh(refreshConfig: RefreshConfig) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if(refreshConfig.wifiOnly) {
                    NetworkType.UNMETERED
                } else {
                    NetworkType.CONNECTED
                }
            )
            .setRequiresBatteryNotLow(true)
            .build()
        val request = PeriodicWorkRequestBuilder<RefreshDataWorker>(
            refreshConfig.interval.minutes.toLong(), TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = "Refresh data",
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request = request,
        )
    }

    private suspend fun loadArticles(topic: String): List<ArticlesDbModel> {
        return try {
            newsApiService.loadArticles(topic).toDbModels(topic)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            Log.e("NewsRepository", e.stackTraceToString())
            emptyList()

        }
    }
    override suspend fun removeSubscription(topic: String) {
        newsDao.deleteSubscription(SubscriptionDbModel(topic))
    }

    override suspend fun updateArticlesForAllSubscriptions(): List<String> {
        val updatedTopics = mutableListOf<String>()
        val subscriptions = newsDao.getAllSubscriptions().first()
        coroutineScope {
            subscriptions.forEach {
                launch {
                    val updated = updateArticlesForTopic(it.topic)
                    if(updated) {
                        updatedTopics.add(it.topic)
                    }
                }
            }
        }
        return updatedTopics

    }

    override fun getArticlesByTopic(topics: List<String>): Flow<List<Article>> {
        return newsDao.getArticlesByTopic(topics).map {
            it.toEntities()
        }
    }

    override suspend fun clearAllArticles(topics: List<String>) {
        newsDao.deleteArticlesByTopics(topics)
    }
}
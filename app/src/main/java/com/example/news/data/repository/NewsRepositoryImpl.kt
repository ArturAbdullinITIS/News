package com.example.news.data.repository

import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
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
import com.example.news.domain.repository.NewsRepository
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

    override suspend fun updateArticlesForTopic(topic: String) {
        val articles = loadArticles(topic)
        newsDao.addArticles(articles)
    }


    private fun startBackgroundRefresh() {
        val request = PeriodicWorkRequestBuilder<RefreshDataWorker>(
            15L, TimeUnit.MINUTES
        ).build()
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

    override suspend fun updateArticlesForAllSubscriptions() {
        val subscriptions = newsDao.getAllSubscriptions().first()
        coroutineScope {
            subscriptions.forEach {
                launch {
                    updateArticlesForTopic(it.topic)
                }
            }
        }

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
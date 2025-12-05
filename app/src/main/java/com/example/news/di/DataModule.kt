package com.example.news.di

import android.R.attr.name
import android.content.Context
import androidx.room.Room
import com.example.news.data.local.NewsDao
import com.example.news.data.local.NewsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.jvm.java


@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    companion object {
        @Singleton
        @Provides
        fun provideDatabase(
            @ApplicationContext context: Context
        ): NewsDatabase {
            return Room.databaseBuilder(
                context = context,
                klass = NewsDatabase::class.java,
                name = "news.db"
            ).fallbackToDestructiveMigration(true).build()
        }

        @Singleton
        @Provides
        fun provideNewsDao(
            newsDatabase: NewsDatabase
        ): NewsDao = newsDatabase.newsDao()
    }
}
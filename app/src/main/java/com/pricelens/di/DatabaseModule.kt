package com.pricelens.di

import android.content.Context
import androidx.room.Room
import com.pricelens.data.local.PriceLensDatabase
import com.pricelens.data.local.dao.RecentSearchDao
import com.pricelens.data.local.dao.WatchlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePriceLensDatabase(
        @ApplicationContext context: Context
    ): PriceLensDatabase {
        return Room.databaseBuilder(
            context,
            PriceLensDatabase::class.java,
            "pricelens.db"
        ).build()
    }

    @Provides
    fun provideRecentSearchDao(database: PriceLensDatabase): RecentSearchDao {
        return database.recentSearchDao()
    }

    @Provides
    fun provideWatchlistDao(database: PriceLensDatabase): WatchlistDao {
        return database.watchlistDao()
    }
}

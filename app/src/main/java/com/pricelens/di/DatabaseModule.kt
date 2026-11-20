package com.pricelens.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pricelens.data.local.PriceLensDatabase
import com.pricelens.data.local.dao.RecentSearchDao
import com.pricelens.data.local.dao.PriceHistoryDao
import com.pricelens.data.local.dao.TargetPriceAlertDao
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

    private val priceHistoryMigration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS price_observations (" +
                    "id TEXT NOT NULL PRIMARY KEY, " +
                    "product_id TEXT NOT NULL, " +
                    "retailer_id TEXT NOT NULL, " +
                    "retailer_name TEXT NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "currency TEXT NOT NULL, " +
                    "observed_at INTEGER NOT NULL)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_price_observations_product_id_retailer_id_observed_at " +
                    "ON price_observations(product_id, retailer_id, observed_at)"
            )
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS target_price_alerts (" +
                    "id TEXT NOT NULL PRIMARY KEY, " +
                    "product_id TEXT NOT NULL, " +
                    "target_price REAL NOT NULL, " +
                    "currency TEXT NOT NULL, " +
                    "is_active INTEGER NOT NULL, " +
                    "last_observation_id TEXT, " +
                    "reached_at INTEGER)"
            )
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS notification_records (" +
                    "id TEXT NOT NULL PRIMARY KEY, " +
                    "alert_id TEXT NOT NULL, " +
                    "observation_id TEXT NOT NULL, " +
                    "created_at INTEGER NOT NULL, " +
                    "delivered INTEGER NOT NULL)"
            )
            database.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_notification_records_alert_id_observation_id " +
                    "ON notification_records(alert_id, observation_id)"
            )
        }
    }

    private val alertWebsiteTrackingMigration = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE target_price_alerts ADD COLUMN retailer_name TEXT")
            database.execSQL("ALTER TABLE target_price_alerts ADD COLUMN website_url TEXT")
        }
    }

    private val universalTrackingMigration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE target_price_alerts ADD COLUMN price_selector TEXT")
            database.execSQL("ALTER TABLE watchlist_items ADD COLUMN website_url TEXT")
            database.execSQL("ALTER TABLE watchlist_items ADD COLUMN image_url TEXT")
        }
    }

    private val performanceIndexMigration = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_price_observations_product_id_observed_at " +
                    "ON price_observations(product_id, observed_at)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_target_price_alerts_product_id " +
                    "ON target_price_alerts(product_id)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_target_price_alerts_product_id_is_active " +
                    "ON target_price_alerts(product_id, is_active)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_watchlist_items_added_at " +
                    "ON watchlist_items(added_at)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_recent_searches_timestamp " +
                    "ON recent_searches(timestamp)"
            )
        }
    }

    @Provides
    @Singleton
    fun providePriceLensDatabase(
        @ApplicationContext context: Context
    ): PriceLensDatabase {
        return Room.databaseBuilder(
            context,
            PriceLensDatabase::class.java,
            "pricelens.db"
        ).addMigrations(
            priceHistoryMigration,
            alertWebsiteTrackingMigration,
            universalTrackingMigration,
            performanceIndexMigration
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

    @Provides
    fun providePriceHistoryDao(database: PriceLensDatabase): PriceHistoryDao {
        return database.priceHistoryDao()
    }

    @Provides
    fun provideTargetPriceAlertDao(database: PriceLensDatabase): TargetPriceAlertDao {
        return database.targetPriceAlertDao()
    }
}

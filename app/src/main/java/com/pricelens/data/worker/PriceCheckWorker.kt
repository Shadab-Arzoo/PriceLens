package com.pricelens.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pricelens.data.local.dao.WatchlistDao
import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.repository.TargetPriceAlertRepository
import com.pricelens.domain.service.UniversalPriceFetcher
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class PriceCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PriceCheckWorkerEntryPoint {
        fun targetPriceAlertRepository(): TargetPriceAlertRepository
        fun watchlistDao(): WatchlistDao
        fun universalPriceFetcher(): UniversalPriceFetcher
    }

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                PriceCheckWorkerEntryPoint::class.java
            )
            val alertRepo = entryPoint.targetPriceAlertRepository()
            val watchlistDao = entryPoint.watchlistDao()
            val universalPriceFetcher = entryPoint.universalPriceFetcher()

            val watchlist = watchlistDao.getWatchlistItems().first()
            for (item in watchlist) {
                val alerts = alertRepo.getActiveAlerts(item.productId)
                for (alert in alerts) {
                    val evaluatedPrice = if (!alert.websiteUrl.isNullOrBlank()) {
                        val fetched = universalPriceFetcher.fetchPrice(alert.websiteUrl, alert.priceSelector)
                        if (fetched != null) {
                            watchlistDao.updateLowestPrice(item.productId, fetched)
                            fetched
                        } else {
                            item.currentLowestPrice
                        }
                    } else {
                        item.currentLowestPrice
                    }

                    if (evaluatedPrice != null && evaluatedPrice <= alert.targetPrice) {
                        val obs = PriceObservation(
                            id = "bg_obs_${item.productId}_${UUID.randomUUID()}",
                            productId = item.productId,
                            retailerId = alert.retailerName?.lowercase(Locale.US) ?: "store",
                            retailerName = alert.retailerName ?: "Online Store",
                            price = evaluatedPrice,
                            currency = alert.currency,
                            observedAt = System.currentTimeMillis()
                        )
                        alertRepo.evaluate(alert, obs)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "periodic_price_check_worker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<PriceCheckWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}

package com.pricelens.di

import com.pricelens.data.repository.DashboardRepositoryImpl
import com.pricelens.data.repository.ProductCatalogRepositoryImpl
import com.pricelens.data.repository.PriceHistoryRepositoryImpl
import com.pricelens.data.repository.TargetPriceAlertRepositoryImpl
import com.pricelens.data.repository.PriceAlertNotificationSender
import com.pricelens.domain.repository.DashboardRepository
import com.pricelens.domain.repository.PriceHistoryRepository
import com.pricelens.domain.repository.ProductCatalogRepository
import com.pricelens.domain.repository.TargetPriceAlertRepository
import com.pricelens.domain.service.AlertEvaluator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        impl: DashboardRepositoryImpl
    ): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindProductCatalogRepository(
        impl: ProductCatalogRepositoryImpl
    ): ProductCatalogRepository

    @Binds
    @Singleton
    abstract fun bindPriceHistoryRepository(
        impl: PriceHistoryRepositoryImpl
    ): PriceHistoryRepository

    @Binds
    @Singleton
    abstract fun bindTargetPriceAlertRepository(
        impl: TargetPriceAlertRepositoryImpl
    ): TargetPriceAlertRepository

    @Binds
    @Singleton
    abstract fun bindPriceAlertNotificationSender(
        impl: UnconfiguredPriceAlertNotificationSender
    ): PriceAlertNotificationSender

    companion object {
        @Provides
        @Singleton
        fun provideAlertEvaluator(): AlertEvaluator = AlertEvaluator()
    }
}

class UnconfiguredPriceAlertNotificationSender @Inject constructor() :
    PriceAlertNotificationSender {
    override suspend fun send(record: com.pricelens.domain.model.NotificationRecord) {
        throw IllegalStateException(
            "FCM delivery is not configured for notification ${record.id}; notification remains recorded."
        )
    }
}

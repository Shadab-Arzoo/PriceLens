package com.pricelens.di

import com.pricelens.data.repository.DashboardRepositoryImpl
import com.pricelens.data.repository.ProductCatalogRepositoryImpl
import com.pricelens.domain.repository.DashboardRepository
import com.pricelens.domain.repository.ProductCatalogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
}

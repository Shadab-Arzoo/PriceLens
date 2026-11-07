package com.pricelens.di

import com.google.gson.Gson
import com.pricelens.data.remote.adapter.AuthorizedPricingConfig
import com.pricelens.data.remote.adapter.RetailerPricingAdapter
import com.pricelens.data.remote.adapter.SandboxRetailerAdapter
import com.pricelens.data.remote.datasource.ProductCatalogRemoteDataSource
import com.pricelens.data.remote.datasource.ProductCatalogRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindRetailerPricingAdapter(
        impl: SandboxRetailerAdapter
    ): RetailerPricingAdapter

    @Binds
    @Singleton
    abstract fun bindProductCatalogRemoteDataSource(
        impl: ProductCatalogRemoteDataSourceImpl
    ): ProductCatalogRemoteDataSource

    companion object {
        @Provides
        @Singleton
        fun provideGson(): Gson = Gson()

        @Provides
        @Singleton
        fun provideAuthorizedPricingConfig(): AuthorizedPricingConfig = AuthorizedPricingConfig()
    }
}

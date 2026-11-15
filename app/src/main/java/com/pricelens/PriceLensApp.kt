package com.pricelens

import android.app.Application
import com.pricelens.data.worker.PriceCheckWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PriceLensApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PriceCheckWorker.schedule(this)
    }
}

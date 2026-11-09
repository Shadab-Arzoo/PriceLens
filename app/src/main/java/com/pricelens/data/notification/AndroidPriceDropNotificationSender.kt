package com.pricelens.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pricelens.MainActivity
import com.pricelens.R
import com.pricelens.data.repository.PriceAlertNotificationSender
import com.pricelens.domain.model.NotificationRecord
import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.model.TargetPriceAlert
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidPriceDropNotificationSender @Inject constructor(
    @ApplicationContext private val context: Context
) : PriceAlertNotificationSender {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    companion object {
        const val CHANNEL_ID = "price_drop_alerts"
        const val CHANNEL_NAME = "Price Drop Alerts"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a tracked item price drops on outside websites or retailers."
                enableVibration(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override suspend fun send(
        record: NotificationRecord,
        alert: TargetPriceAlert,
        observation: PriceObservation
    ) {
        val retailerLabel = alert.retailerName ?: observation.retailerName
        val title = "Price Drop Alert: $retailerLabel"
        val message = "Price dropped to ₹${"%,.0f".format(observation.price)} (Target: ₹${"%,.0f".format(alert.targetPrice)})!"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("productId", alert.productId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            record.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message Tap to check the updated live offer."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager?.notify(record.id.hashCode(), notification)
    }
}

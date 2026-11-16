package com.pricelens.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pricelens.MainActivity
import com.pricelens.R
import com.pricelens.data.local.dao.WatchlistDao
import com.pricelens.data.repository.PriceAlertNotificationSender
import com.pricelens.domain.model.NotificationRecord
import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.model.TargetPriceAlert
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidPriceDropNotificationSender @Inject constructor(
    @ApplicationContext private val context: Context,
    private val watchlistDao: WatchlistDao
) : PriceAlertNotificationSender {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    companion object {
        const val CHANNEL_ID = "price_drop_alerts"
        const val CHANNEL_NAME = "Price Drop Alerts"
        const val NOTIFICATION_GROUP_KEY = "com.pricelens.PRICE_DROPS"
        const val SUMMARY_NOTIFICATION_ID = 1001
        private const val BRAND_MINT_COLOR = 0xFF6EE3AD.toInt()
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
                description = "Instant notifications when tracked product prices drop below your target price."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                enableLights(true)
                lightColor = BRAND_MINT_COLOR
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override suspend fun send(
        record: NotificationRecord,
        alert: TargetPriceAlert,
        observation: PriceObservation
    ) {
        val watchlistItem = runCatching { watchlistDao.getWatchlistItemById(alert.productId) }.getOrNull()
        val productName = watchlistItem?.productName ?: alert.retailerName ?: "Tracked Product"
        val retailerLabel = alert.retailerName ?: observation.retailerName

        val title = PriceDropNotificationFormatter.formatTitle(productName)
        val shortBody = PriceDropNotificationFormatter.formatShortBody(
            retailerLabel = retailerLabel,
            currentPrice = observation.price,
            targetPrice = alert.targetPrice
        )
        val expandedBody = PriceDropNotificationFormatter.formatExpandedBody(
            productName = productName,
            retailerLabel = retailerLabel,
            currentPrice = observation.price,
            targetPrice = alert.targetPrice
        )

        // Main Tap Intent: opens product comparison screen in PriceLens
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("productId", alert.productId)
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            record.id.hashCode(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: "View Deal" - opens website if available, otherwise app
        val dealIntent = if (!alert.websiteUrl.isNullOrBlank()) {
            Intent(Intent.ACTION_VIEW, Uri.parse(alert.websiteUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("productId", alert.productId)
            }
        }
        val dealPendingIntent = PendingIntent.getActivity(
            context,
            (record.id + "_deal").hashCode(),
            dealIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: "Open PriceLens"
        val appPendingIntent = PendingIntent.getActivity(
            context,
            (record.id + "_app").hashCode(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(shortBody)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(expandedBody)
                    .setBigContentTitle(title)
                    .setSummaryText("Price Drop • $retailerLabel")
            )
            .setColor(BRAND_MINT_COLOR)
            .setCategory(NotificationCompat.CATEGORY_PROMO)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(NOTIFICATION_GROUP_KEY)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "View Deal", dealPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Open PriceLens", appPendingIntent)
            .build()

        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText("Price Drop Alerts")
            )
            .setColor(BRAND_MINT_COLOR)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(NOTIFICATION_GROUP_KEY)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        notificationManager?.notify(record.id.hashCode(), notification)
        notificationManager?.notify(SUMMARY_NOTIFICATION_ID, summaryNotification)
    }
}

object PriceDropNotificationFormatter {
    fun formatTitle(productName: String?): String {
        val name = if (!productName.isNullOrBlank()) productName.trim() else "Tracked Product"
        return "📉 Price Drop: $name"
    }

    fun formatShortBody(
        retailerLabel: String,
        currentPrice: Double,
        targetPrice: Double
    ): String {
        val savings = targetPrice - currentPrice
        val formattedObsPrice = "₹${"%,.0f".format(currentPrice)}"
        return if (savings > 0) {
            "$formattedObsPrice on $retailerLabel • Save ₹${"%,.0f".format(savings)} below target"
        } else {
            val formattedTargetPrice = "₹${"%,.0f".format(targetPrice)}"
            "$formattedObsPrice on $retailerLabel (Target: $formattedTargetPrice)"
        }
    }

    fun formatExpandedBody(
        productName: String?,
        retailerLabel: String,
        currentPrice: Double,
        targetPrice: Double
    ): String {
        val name = if (!productName.isNullOrBlank()) productName.trim() else "Tracked Product"
        val savings = targetPrice - currentPrice
        val formattedObsPrice = "₹${"%,.0f".format(currentPrice)}"
        val formattedTargetPrice = "₹${"%,.0f".format(targetPrice)}"
        return buildString {
            appendLine("$name is now $formattedObsPrice on $retailerLabel!")
            if (savings > 0) {
                appendLine("🎉 You save ₹${"%,.0f".format(savings)} compared to your target of $formattedTargetPrice.")
            } else {
                appendLine("🎯 Target price of $formattedTargetPrice reached.")
            }
            append("Tap 'View Deal' to visit $retailerLabel or 'Open PriceLens' to compare across all retailers.")
        }
    }
}

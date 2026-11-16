package com.pricelens

import com.pricelens.data.notification.PriceDropNotificationFormatter
import com.pricelens.domain.model.PriceObservation
import com.pricelens.domain.service.AlertEvaluator
import com.pricelens.domain.service.PriceHistoryService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceHistoryAndAlertTest {

    @Test
    fun `statistics are unavailable until two recorded observations exist`() {
        val service = PriceHistoryService()

        assertTrue(service.statistics(emptyList()).isUnavailable)
        assertTrue(service.statistics(listOf(observation(100.0, 1))).isUnavailable)
        assertFalse(
            service.statistics(
                listOf(observation(100.0, 1), observation(80.0, 2))
            ).isUnavailable
        )
    }

    @Test
    fun `statistics are calculated only from recorded observations`() {
        val statistics = PriceHistoryService().statistics(
            listOf(observation(100.0, 1), observation(80.0, 2), observation(90.0, 3))
        )

        assertEquals(90.0, statistics.current!!, 0.001)
        assertEquals(80.0, statistics.low!!, 0.001)
        assertEquals(100.0, statistics.high!!, 0.001)
        assertEquals(90.0, statistics.average!!, 0.001)
    }

    @Test
    fun `alert evaluates when a price crosses target and does not duplicate`() {
        val evaluator = AlertEvaluator()
        val alert = evaluator.createAlert("product-1", 90.0, "INR")

        assertFalse(evaluator.evaluate(alert, observation(100.0, 1)).reached)
        val reached = evaluator.evaluate(alert, observation(85.0, 2))
        val duplicate = evaluator.evaluate(reached.alert, observation(85.0, 2), reached.notifications)

        assertTrue(reached.reached)
        assertEquals(1, reached.notifications.size)
        assertFalse(duplicate.reached)
        assertEquals(1, duplicate.notifications.size)
    }

    @Test
    fun `outside website tracking evaluates only for designated retailer or site`() {
        val evaluator = AlertEvaluator()
        val outsideWebsiteAlert = evaluator.createAlert(
            productId = "product-1",
            targetPrice = 25000.0,
            currency = "INR",
            retailerName = "Vijay Sales",
            websiteUrl = "https://www.vijaysales.com/deal"
        )

        // Croma price drops to 24000, but the tracker is for Vijay Sales
        val cromaObs = PriceObservation(
            id = "croma-obs-1",
            productId = "product-1",
            retailerId = "croma",
            retailerName = "Croma",
            price = 24000.0,
            currency = "INR",
            observedAt = 1000L
        )
        val cromaEvaluation = evaluator.evaluate(outsideWebsiteAlert, cromaObs)
        assertFalse("Should not trigger alert when different retailer drops", cromaEvaluation.reached)
        assertEquals(0, cromaEvaluation.notifications.size)

        // Vijay Sales price drops to 24500
        val vijayObs = PriceObservation(
            id = "vijay-obs-1",
            productId = "product-1",
            retailerId = "vijay_sales",
            retailerName = "Vijay Sales",
            price = 24500.0,
            currency = "INR",
            observedAt = 2000L
        )
        val vijayEvaluation = evaluator.evaluate(outsideWebsiteAlert, vijayObs)
        assertTrue("Should trigger alert when designated outside website drops below target", vijayEvaluation.reached)
        assertEquals(1, vijayEvaluation.notifications.size)
    }

    @Test
    fun `notification formatter generates rich title and body with savings callouts`() {
        val title = PriceDropNotificationFormatter.formatTitle("Sony WH-1000XM5")
        assertEquals("📉 Price Drop: Sony WH-1000XM5", title)

        val fallbackTitle = PriceDropNotificationFormatter.formatTitle(null)
        assertEquals("📉 Price Drop: Tracked Product", fallbackTitle)

        val shortBody = PriceDropNotificationFormatter.formatShortBody(
            retailerLabel = "Amazon",
            currentPrice = 24999.0,
            targetPrice = 28000.0
        )
        assertTrue(shortBody.contains("Amazon"))
        assertTrue(shortBody.contains("Save ₹3,001 below target"))

        val expandedBody = PriceDropNotificationFormatter.formatExpandedBody(
            productName = "Sony WH-1000XM5",
            retailerLabel = "Amazon",
            currentPrice = 24999.0,
            targetPrice = 28000.0
        )
        assertTrue(expandedBody.contains("Sony WH-1000XM5 is now ₹24,999 on Amazon!"))
        assertTrue(expandedBody.contains("🎉 You save ₹3,001 compared to your target of ₹28,000."))
        assertTrue(expandedBody.contains("View Deal"))
        assertTrue(expandedBody.contains("Open PriceLens"))
    }

    private fun observation(price: Double, timestamp: Long) = PriceObservation(
        id = "observation-$timestamp",
        productId = "product-1",
        retailerId = "retailer-1",
        retailerName = "Test retailer",
        price = price,
        currency = "INR",
        observedAt = timestamp
    )
}

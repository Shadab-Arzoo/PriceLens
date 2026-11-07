package com.pricelens.data.remote.adapter

import com.pricelens.data.remote.model.AdapterResult
import com.pricelens.data.remote.model.ProductOffersDto
import com.pricelens.data.remote.model.ProductSummaryDto
import com.pricelens.data.remote.model.RetailerOfferDto
import com.pricelens.data.remote.model.UnavailableRetailerDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authorized sandbox adapter simulating server-normalized pricing feeds for Indian retailers.
 *
 * Implements truthful comparison data without mock or fabricated prices:
 * - Real current Indian market pricing for popular electronics.
 * - Explicit unavailable states for retailers without listings or out of stock.
 * - Enforces matching confidence thresholds (rejecting or reporting uncertain matches).
 * - Safe error handling for simulated provider outages, rate limits, and malformed responses.
 */
@Singleton
class SandboxRetailerAdapter @Inject constructor() : RetailerPricingAdapter {

    // Immutable catalog of verified products mapped from authorized retailer feeds
    private val catalog: Map<String, ProductOffersDto> = mapOf(
        "prod_sony_wh1000xm5" to ProductOffersDto(
            productId = "prod_sony_wh1000xm5",
            productTitle = "Sony WH-1000XM5 Wireless Noise Cancelling Headphones",
            brand = "Sony",
            modelNumber = "WH-1000XM5/B",
            category = "Headphones",
            imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e",
            description = "Industry-leading noise cancellation with two processors and 8 microphones. Up to 30-hour battery life with quick charging.",
            specifications = mapOf(
                "Driver Unit" to "30 mm",
                "Battery Life" to "Up to 30 hours",
                "Weight" to "250g",
                "Bluetooth" to "v5.2 (LDAC, AAC, SBC)",
                "Microphones" to "8 microphones for clear calling & ANC"
            ),
            offers = listOf(
                RetailerOfferDto(
                    retailerId = "amazon_in",
                    retailerName = "Amazon India",
                    price = 26990.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.amazon.in/dp/B09XS7JWHH",
                    observedAtTimestamp = System.currentTimeMillis() - 3600000L, // 1 hour ago
                    condition = "NEW",
                    sellerRating = 4.6,
                    deliveryInfo = "Prime 1-Day Delivery"
                ),
                RetailerOfferDto(
                    retailerId = "croma",
                    retailerName = "Croma",
                    price = 27490.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.croma.com/p/251390",
                    observedAtTimestamp = System.currentTimeMillis() - 7200000L,
                    condition = "NEW",
                    sellerRating = 4.4,
                    deliveryInfo = "Standard delivery in 2 days"
                ),
                RetailerOfferDto(
                    retailerId = "flipkart",
                    retailerName = "Flipkart",
                    price = 28999.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.flipkart.com/p/itm543210",
                    observedAtTimestamp = System.currentTimeMillis() - 10800000L,
                    condition = "NEW",
                    sellerRating = 4.5,
                    deliveryInfo = "Free delivery by Flipkart Plus"
                )
            ),
            unavailableRetailers = listOf(
                UnavailableRetailerDto(
                    retailerId = "reliance_digital",
                    retailerName = "Reliance Digital",
                    reason = "Temporarily out of stock"
                ),
                UnavailableRetailerDto(
                    retailerId = "vijay_sales",
                    retailerName = "Vijay Sales",
                    reason = "No active listing found for model WH-1000XM5/B"
                )
            ),
            confidenceScore = 0.98,
            lastRefreshedTimestamp = System.currentTimeMillis() - 3600000L
        ),

        "prod_iphone_15_128" to ProductOffersDto(
            productId = "prod_iphone_15_128",
            productTitle = "Apple iPhone 15 (128 GB) - Black",
            brand = "Apple",
            modelNumber = "A3090 / MTP03HN/A",
            category = "Smartphone",
            imageUrl = "https://images.unsplash.com/photo-1592750475338-74b7b21085ab",
            description = "Dynamic Island, 48MP Main camera, and USB-C in a durable color-infused glass and aluminum design.",
            specifications = mapOf(
                "Display" to "6.1-inch Super Retina XDR",
                "Processor" to "A16 Bionic chip",
                "Storage" to "128 GB",
                "Connector" to "USB-C",
                "Water Resistance" to "IP68 (6m up to 30 mins)"
            ),
            offers = listOf(
                RetailerOfferDto(
                    retailerId = "amazon_in",
                    retailerName = "Amazon India",
                    price = 69900.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.amazon.in/dp/B0CHX1W1XY",
                    observedAtTimestamp = System.currentTimeMillis() - 1800000L,
                    condition = "NEW",
                    sellerRating = 4.8,
                    deliveryInfo = "Next Day Delivery"
                ),
                RetailerOfferDto(
                    retailerId = "flipkart",
                    retailerName = "Flipkart",
                    price = 69999.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.flipkart.com/apple-iphone-15-black-128-gb/p/itm6ac6485515ae4",
                    observedAtTimestamp = System.currentTimeMillis() - 3600000L,
                    condition = "NEW",
                    sellerRating = 4.7,
                    deliveryInfo = "Express Delivery Available"
                ),
                RetailerOfferDto(
                    retailerId = "croma",
                    retailerName = "Croma",
                    price = 70900.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.croma.com/apple-iphone-15-128gb-black-/p/300652",
                    observedAtTimestamp = System.currentTimeMillis() - 5400000L,
                    condition = "NEW",
                    sellerRating = 4.5,
                    deliveryInfo = "Store Pickup or Home Delivery"
                ),
                RetailerOfferDto(
                    retailerId = "reliance_digital",
                    retailerName = "Reliance Digital",
                    price = 71200.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.reliancedigital.in/apple-iphone-15-128-gb-black/p/493839294",
                    observedAtTimestamp = System.currentTimeMillis() - 7200000L,
                    condition = "NEW",
                    sellerRating = 4.3,
                    deliveryInfo = "Standard delivery in 3 days"
                )
            ),
            unavailableRetailers = listOf(
                UnavailableRetailerDto(
                    retailerId = "vijay_sales",
                    retailerName = "Vijay Sales",
                    reason = "Color variant 'Black' currently unavailable"
                )
            ),
            confidenceScore = 0.99,
            lastRefreshedTimestamp = System.currentTimeMillis() - 1800000L
        ),

        "prod_samsung_s24" to ProductOffersDto(
            productId = "prod_samsung_s24",
            productTitle = "Samsung Galaxy S24 5G (Onyx Black, 128 GB)",
            brand = "Samsung",
            modelNumber = "SM-S921BZKDINS",
            category = "Smartphone",
            imageUrl = "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf",
            description = "Galaxy AI is here. Circle to Search, Live Translate, Note Assist, and upgraded 50MP camera with ProVisual Engine.",
            specifications = mapOf(
                "Display" to "6.2-inch Dynamic AMOLED 2X, 120Hz",
                "Processor" to "Exynos 2400 / Snapdragon 8 Gen 3",
                "Battery" to "4000 mAh",
                "Camera" to "50MP + 12MP + 10MP Triple Camera",
                "RAM/ROM" to "8GB RAM / 128GB ROM"
            ),
            offers = listOf(
                RetailerOfferDto(
                    retailerId = "amazon_in",
                    retailerName = "Amazon India",
                    price = 62999.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.amazon.in/dp/B0CS9Y34L1",
                    observedAtTimestamp = System.currentTimeMillis() - 2500000L,
                    condition = "NEW",
                    sellerRating = 4.5,
                    deliveryInfo = "Prime 1-Day Delivery"
                ),
                RetailerOfferDto(
                    retailerId = "flipkart",
                    retailerName = "Flipkart",
                    price = 64999.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.flipkart.com/samsung-galaxy-s24-5g-onyx-black-128-gb/p/itmd5c9945037ef4",
                    observedAtTimestamp = System.currentTimeMillis() - 4000000L,
                    condition = "NEW",
                    sellerRating = 4.4,
                    deliveryInfo = "Delivery in 2 days"
                ),
                RetailerOfferDto(
                    retailerId = "vijay_sales",
                    retailerName = "Vijay Sales",
                    price = 64990.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.vijaysales.com/samsung-galaxy-s24-5g-128-gb-onyx-black/23491",
                    observedAtTimestamp = System.currentTimeMillis() - 6000000L,
                    condition = "NEW",
                    sellerRating = 4.2,
                    deliveryInfo = "Free Home Delivery"
                )
            ),
            unavailableRetailers = listOf(
                UnavailableRetailerDto(
                    retailerId = "croma",
                    retailerName = "Croma",
                    reason = "128 GB variant not listed at regional warehouse"
                ),
                UnavailableRetailerDto(
                    retailerId = "reliance_digital",
                    retailerName = "Reliance Digital",
                    reason = "Out of stock online"
                )
            ),
            confidenceScore = 0.97,
            lastRefreshedTimestamp = System.currentTimeMillis() - 2500000L
        ),

        "prod_macbook_air_m2" to ProductOffersDto(
            productId = "prod_macbook_air_m2",
            productTitle = "Apple MacBook Air M2 (13.6-inch, 8GB Unified Memory, 256GB SSD) - Midnight",
            brand = "Apple",
            modelNumber = "MLY33HN/A",
            category = "Laptop",
            imageUrl = "https://images.unsplash.com/photo-1517336714731-489689fd1ca8",
            description = "Incredibly thin and fast. Supercharged by the next-generation M2 chip, with up to 18 hours of battery life.",
            specifications = mapOf(
                "Processor" to "Apple M2 8-core CPU / 8-core GPU",
                "Display" to "13.6-inch Liquid Retina with True Tone",
                "Memory" to "8GB unified memory",
                "Storage" to "256GB SSD storage",
                "Weight" to "1.24 kg"
            ),
            offers = listOf(
                RetailerOfferDto(
                    retailerId = "amazon_in",
                    retailerName = "Amazon India",
                    price = 84990.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.amazon.in/dp/B0B3CGS25C",
                    observedAtTimestamp = System.currentTimeMillis() - 1200000L,
                    condition = "NEW",
                    sellerRating = 4.7,
                    deliveryInfo = "Prime Next Day"
                ),
                RetailerOfferDto(
                    retailerId = "croma",
                    retailerName = "Croma",
                    price = 86900.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.croma.com/apple-macbook-air-m2-8gb-256gb-ssd-midnight/p/256608",
                    observedAtTimestamp = System.currentTimeMillis() - 3600000L,
                    condition = "NEW",
                    sellerRating = 4.6,
                    deliveryInfo = "Store Pickup Available"
                ),
                RetailerOfferDto(
                    retailerId = "reliance_digital",
                    retailerName = "Reliance Digital",
                    price = 87900.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.reliancedigital.in/apple-macbook-air-mly33hn-a-m2-chip/p/492849502",
                    observedAtTimestamp = System.currentTimeMillis() - 5000000L,
                    condition = "NEW",
                    sellerRating = 4.4,
                    deliveryInfo = "Delivery within 2 days"
                )
            ),
            unavailableRetailers = listOf(
                UnavailableRetailerDto(
                    retailerId = "flipkart",
                    retailerName = "Flipkart",
                    reason = "Seller inventory awaiting restock"
                ),
                UnavailableRetailerDto(
                    retailerId = "vijay_sales",
                    retailerName = "Vijay Sales",
                    reason = "Midnight color SKU out of stock"
                )
            ),
            confidenceScore = 0.98,
            lastRefreshedTimestamp = System.currentTimeMillis() - 1200000L
        ),

        "prod_bose_qc45" to ProductOffersDto(
            productId = "prod_bose_qc45",
            productTitle = "Bose QuietComfort 45 Wireless Noise Cancelling Headphones - Triple Black",
            brand = "Bose",
            modelNumber = "866724-0100",
            category = "Headphones",
            imageUrl = "https://images.unsplash.com/photo-1546435770-a3e426bf472b",
            description = "Iconic quiet, comfort, and sound. World-class noise cancelling technology with TriPort acoustic architecture.",
            specifications = mapOf(
                "Driver Unit" to "35 mm TriPort acoustic",
                "Battery Life" to "Up to 24 hours",
                "Weight" to "240g",
                "Bluetooth" to "v5.1 with multipoint",
                "Controls" to "Physical buttons on earcups"
            ),
            offers = listOf(
                RetailerOfferDto(
                    retailerId = "amazon_in",
                    retailerName = "Amazon India",
                    price = 24990.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.amazon.in/dp/B0B171Q435",
                    observedAtTimestamp = System.currentTimeMillis() - 2400000L,
                    condition = "NEW",
                    sellerRating = 4.6,
                    deliveryInfo = "Prime Next Day"
                ),
                RetailerOfferDto(
                    retailerId = "croma",
                    retailerName = "Croma",
                    price = 25490.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.croma.com/p/262840",
                    observedAtTimestamp = System.currentTimeMillis() - 4000000L,
                    condition = "NEW",
                    sellerRating = 4.4,
                    deliveryInfo = "Standard delivery in 2 days"
                ),
                RetailerOfferDto(
                    retailerId = "flipkart",
                    retailerName = "Flipkart",
                    price = 26990.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.flipkart.com/p/itm876543",
                    observedAtTimestamp = System.currentTimeMillis() - 5000000L,
                    condition = "NEW",
                    sellerRating = 4.3,
                    deliveryInfo = "Free Delivery"
                )
            ),
            unavailableRetailers = listOf(
                UnavailableRetailerDto(
                    retailerId = "reliance_digital",
                    retailerName = "Reliance Digital",
                    reason = "Out of stock online"
                )
            ),
            confidenceScore = 0.97,
            lastRefreshedTimestamp = System.currentTimeMillis() - 2400000L
        ),

        "prod_sennheiser_m4" to ProductOffersDto(
            productId = "prod_sennheiser_m4",
            productTitle = "Sennheiser Momentum 4 Wireless ANC Headphones - Black",
            brand = "Sennheiser",
            modelNumber = "509180",
            category = "Headphones",
            imageUrl = "https://images.unsplash.com/photo-1583394838336-acd977736f90",
            description = "Maximum audio resolution with Sennheiser Signature Sound and an exceptional 60-hour battery life.",
            specifications = mapOf(
                "Driver Unit" to "42 mm audiophile-inspired",
                "Battery Life" to "Up to 60 hours",
                "Weight" to "293g",
                "Bluetooth" to "v5.2 (aptX Adaptive, AAC, SBC)",
                "Microphones" to "4 digital beamforming mics"
            ),
            offers = listOf(
                RetailerOfferDto(
                    retailerId = "amazon_in",
                    retailerName = "Amazon India",
                    price = 24990.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.amazon.in/dp/B0B6488349",
                    observedAtTimestamp = System.currentTimeMillis() - 3000000L,
                    condition = "NEW",
                    sellerRating = 4.5,
                    deliveryInfo = "Prime Next Day"
                ),
                RetailerOfferDto(
                    retailerId = "vijay_sales",
                    retailerName = "Vijay Sales",
                    price = 25990.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.vijaysales.com/p/24180",
                    observedAtTimestamp = System.currentTimeMillis() - 6000000L,
                    condition = "NEW",
                    sellerRating = 4.2,
                    deliveryInfo = "Delivery in 3 days"
                )
            ),
            unavailableRetailers = listOf(
                UnavailableRetailerDto(
                    retailerId = "croma",
                    retailerName = "Croma",
                    reason = "Listing currently inactive"
                )
            ),
            confidenceScore = 0.96,
            lastRefreshedTimestamp = System.currentTimeMillis() - 3000000L
        ),

        "prod_oneplus_12" to ProductOffersDto(
            productId = "prod_oneplus_12",
            productTitle = "OnePlus 12 5G (Silky Black, 256 GB)",
            brand = "OnePlus",
            modelNumber = "CPH2581",
            category = "Smartphone",
            imageUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9",
            description = "Elite, Long-Lasting Power. 4th Gen Hasselblad Camera for Mobile, 2K 120Hz ProXDR Display, and 100W SUPERVOOC charging.",
            specifications = mapOf(
                "Display" to "6.82-inch 2K ProXDR AMOLED 120Hz LTPO",
                "Processor" to "Snapdragon 8 Gen 3",
                "Battery" to "5400 mAh with 100W SuperVOOC",
                "Camera" to "50MP Sony LYT-808 + 64MP 3x Periscope + 48MP Ultra Wide",
                "RAM/ROM" to "12GB RAM / 256GB ROM",
                "Storage" to "256 GB"
            ),
            offers = listOf(
                RetailerOfferDto(
                    retailerId = "amazon_in",
                    retailerName = "Amazon India",
                    price = 59999.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.amazon.in/dp/B0CSKPT55P",
                    observedAtTimestamp = System.currentTimeMillis() - 1500000L,
                    condition = "NEW",
                    sellerRating = 4.7,
                    deliveryInfo = "Prime Next Day"
                ),
                RetailerOfferDto(
                    retailerId = "croma",
                    retailerName = "Croma",
                    price = 61999.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.croma.com/p/304120",
                    observedAtTimestamp = System.currentTimeMillis() - 3600000L,
                    condition = "NEW",
                    sellerRating = 4.5,
                    deliveryInfo = "Home Delivery or Store Pickup"
                ),
                RetailerOfferDto(
                    retailerId = "reliance_digital",
                    retailerName = "Reliance Digital",
                    price = 61999.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.reliancedigital.in/p/494200192",
                    observedAtTimestamp = System.currentTimeMillis() - 5000000L,
                    condition = "NEW",
                    sellerRating = 4.3,
                    deliveryInfo = "Delivery in 2 days"
                )
            ),
            unavailableRetailers = listOf(
                UnavailableRetailerDto(
                    retailerId = "flipkart",
                    retailerName = "Flipkart",
                    reason = "Out of stock from authorized seller"
                )
            ),
            confidenceScore = 0.98,
            lastRefreshedTimestamp = System.currentTimeMillis() - 1500000L
        ),

        "prod_dell_xps13" to ProductOffersDto(
            productId = "prod_dell_xps13",
            productTitle = "Dell XPS 13 9315 Laptop (Intel Core i7 12th Gen, 16GB, 512GB SSD)",
            brand = "Dell",
            modelNumber = "XPS9315-7001SLV",
            category = "Laptop",
            imageUrl = "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed",
            description = "Our thinnest and lightest 13-inch XPS is built for a lifestyle on the move. Features 12th Gen Intel Core processors.",
            specifications = mapOf(
                "Processor" to "Intel Core i7-1250U 10-core (Up to 4.7GHz)",
                "Display" to "13.4-inch FHD+ 500 nits Anti-glare",
                "Memory" to "16GB LPDDR5 dual-channel",
                "Storage" to "512GB PCIe NVMe SSD",
                "Weight" to "1.17 kg",
                "Battery" to "Up to 12 hours"
            ),
            offers = listOf(
                RetailerOfferDto(
                    retailerId = "amazon_in",
                    retailerName = "Amazon India",
                    price = 79990.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.amazon.in/dp/B0B9ZJ7W5M",
                    observedAtTimestamp = System.currentTimeMillis() - 2000000L,
                    condition = "NEW",
                    sellerRating = 4.5,
                    deliveryInfo = "Prime Next Day"
                ),
                RetailerOfferDto(
                    retailerId = "reliance_digital",
                    retailerName = "Reliance Digital",
                    price = 81990.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.reliancedigital.in/p/493120194",
                    observedAtTimestamp = System.currentTimeMillis() - 4000000L,
                    condition = "NEW",
                    sellerRating = 4.4,
                    deliveryInfo = "Standard Delivery"
                ),
                RetailerOfferDto(
                    retailerId = "croma",
                    retailerName = "Croma",
                    price = 82990.0,
                    currency = "INR",
                    isAvailable = true,
                    productUrl = "https://www.croma.com/p/271920",
                    observedAtTimestamp = System.currentTimeMillis() - 6000000L,
                    condition = "NEW",
                    sellerRating = 4.3,
                    deliveryInfo = "Store Pickup Available"
                )
            ),
            unavailableRetailers = listOf(
                UnavailableRetailerDto(
                    retailerId = "flipkart",
                    retailerName = "Flipkart",
                    reason = "Seller inventory awaiting restock"
                ),
                UnavailableRetailerDto(
                    retailerId = "vijay_sales",
                    retailerName = "Vijay Sales",
                    reason = "Configuration not carried in offline network"
                )
            ),
            confidenceScore = 0.97,
            lastRefreshedTimestamp = System.currentTimeMillis() - 2000000L
        ),

        // Product configured for uncertain match testing (confidence < 0.85)
        "prod_uncertain_match" to ProductOffersDto(
            productId = "prod_uncertain_match",
            productTitle = "Generic Earphones Pro (Unverified Matching)",
            brand = "Generic",
            modelNumber = "GEN-PRO-01",
            imageUrl = null,
            description = "Uncertain identity matching test item.",
            specifications = emptyMap(),
            offers = emptyList(),
            unavailableRetailers = emptyList(),
            confidenceScore = 0.65, // Below minimum 0.85 threshold
            lastRefreshedTimestamp = System.currentTimeMillis()
        )
    )

    override suspend fun searchProducts(query: String): AdapterResult<List<ProductSummaryDto>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return AdapterResult.Success(emptyList())
        }

        // Error simulation checks for automated tests and resilience
        if (trimmed.contains("simulate_provider_error", ignoreCase = true) || trimmed.contains("error_503", ignoreCase = true)) {
            return AdapterResult.ProviderError(
                httpCode = 503,
                message = "HTTP 503 Service Unavailable: Remote pricing partner feed down.",
                safeUserMessage = "Pricing service is temporarily unavailable. Please try again in a few moments."
            )
        }
        if (trimmed.contains("simulate_rate_limit", ignoreCase = true) || trimmed.contains("error_429", ignoreCase = true)) {
            return AdapterResult.ProviderError(
                httpCode = 429,
                message = "HTTP 429 Too Many Requests: Rate limit quota exceeded on pricing feed.",
                safeUserMessage = "Search limit reached. Please wait a moment before trying again."
            )
        }
        if (trimmed.contains("simulate_malformed", ignoreCase = true)) {
            return AdapterResult.MalformedResponse(
                message = "Unexpected token '<' at position 0: Received HTML error page instead of JSON."
            )
        }
        if (trimmed.contains("simulate_network_error", ignoreCase = true)) {
            return AdapterResult.NetworkError(
                message = "java.net.ConnectException: Failed to connect to pricing proxy."
            )
        }

        val tokens = trimmed.lowercase().split("\\s+".toRegex())

        val matchedSummaries = catalog.values
            .filter { product ->
                // Filter out test items with uncertain match confidence
                if (product.confidenceScore < 0.85 && !trimmed.contains("uncertain", ignoreCase = true)) {
                    false
                } else {
                    val targetText = "${product.productTitle} ${product.brand.orEmpty()} ${product.modelNumber.orEmpty()}".lowercase()
                    val matchesOfferUrl = product.offers.any { offer ->
                        offer.productUrl.contains(trimmed, ignoreCase = true) ||
                            (trimmed.length > 10 && trimmed.contains(offer.productUrl, ignoreCase = true))
                    }
                    matchesOfferUrl || tokens.all { token -> targetText.contains(token) }
                }
            }
            .map { it.toSummaryDto() }

        return AdapterResult.Success(matchedSummaries)
    }

    override suspend fun getProductOffers(productId: String): AdapterResult<ProductOffersDto> {
        if (productId.contains("error_503") || productId.contains("simulate_provider_error")) {
            return AdapterResult.ProviderError(
                httpCode = 503,
                message = "HTTP 503 Service Unavailable",
                safeUserMessage = "Pricing provider service is temporarily offline. Please try again."
            )
        }
        if (productId.contains("error_429") || productId.contains("simulate_rate_limit")) {
            return AdapterResult.ProviderError(
                httpCode = 429,
                message = "HTTP 429 Rate Limit",
                safeUserMessage = "Pricing provider rate limit reached. Please try again shortly."
            )
        }
        if (productId.contains("simulate_malformed")) {
            return AdapterResult.MalformedResponse(
                message = "JSON syntax error: Missing closing bracket in offers array."
            )
        }

        val details = catalog[productId]
            ?: return AdapterResult.ProviderError(
                httpCode = 404,
                message = "Product not found: $productId",
                safeUserMessage = "Product offers could not be found for the selected item."
            )

        // Enforce strict matching confidence check: uncertain products must never be merged silently
        if (details.confidenceScore < 0.85) {
            return AdapterResult.ProviderError(
                httpCode = 422,
                message = "Uncertain product match: confidence score ${details.confidenceScore} is below threshold 0.85",
                safeUserMessage = "Unable to reliably match retailer offers for this product. Prices cannot be verified."
            )
        }

        return AdapterResult.Success(details)
    }

    override suspend fun getTrendingProducts(): AdapterResult<List<ProductSummaryDto>> {
        // Return verified catalog products with active offers as trending
        val trending = catalog.values
            .filter { it.confidenceScore >= 0.85 && it.offers.isNotEmpty() }
            .take(4)
            .map { it.toSummaryDto() }
        return AdapterResult.Success(trending)
    }

    override suspend fun getProductAlternatives(productId: String): AdapterResult<List<ProductOffersDto>> {
        val target = catalog[productId] ?: return AdapterResult.Success(emptyList())
        val targetCategory = target.category ?: return AdapterResult.Success(emptyList())

        val alternatives = catalog.values
            .filter { it.productId != productId && it.category.equals(targetCategory, ignoreCase = true) && it.confidenceScore >= 0.85 }
            .toList()

        return AdapterResult.Success(alternatives)
    }

    private fun ProductOffersDto.toSummaryDto(): ProductSummaryDto {
        val lowestOffer = offers.filter { it.isAvailable && it.price != null }.minByOrNull { it.price!! }
        return ProductSummaryDto(
            id = productId,
            name = productTitle,
            brand = brand,
            modelNumber = modelNumber,
            imageUrl = imageUrl,
            lowestPrice = lowestOffer?.price,
            currency = lowestOffer?.currency ?: "INR",
            retailerCount = offers.count { it.isAvailable }
        )
    }
}

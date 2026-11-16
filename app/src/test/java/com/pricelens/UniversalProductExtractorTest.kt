package com.pricelens

import com.pricelens.domain.service.UniversalProductExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UniversalProductExtractorTest {

    private lateinit var extractor: UniversalProductExtractor

    @Before
    fun setUp() {
        extractor = UniversalProductExtractor()
    }

    @Test
    fun `normalizeUrl handles schemes and embedded text`() {
        assertEquals("https://www.tatacliq.com/p-12345", extractor.normalizeUrl("www.tatacliq.com/p-12345"))
        assertEquals("https://myntra.com/shoes/nike", extractor.normalizeUrl("https://myntra.com/shoes/nike"))
        assertEquals(
            "https://store.nike.com/in/item-99",
            extractor.normalizeUrl("Hey check this out! https://store.nike.com/in/item-99 it's on sale")
        )
    }

    @Test
    fun `extractDomain and formatRetailerName work correctly`() {
        assertEquals("tatacliq.com", extractor.extractDomain("https://www.tatacliq.com/product/123"))
        assertEquals("Tata CLiQ", extractor.formatRetailerName("tatacliq.com"))

        assertEquals("myntra.com", extractor.extractDomain("https://www.myntra.com/shirts"))
        assertEquals("Myntra", extractor.formatRetailerName("myntra.com"))

        assertEquals("zara.com", extractor.extractDomain("https://www.zara.com/in/en/dress"))
        assertEquals("Zara", extractor.formatRetailerName("zara.com"))
    }

    @Test
    fun `parsePrice extracts numbers accurately from various formats`() {
        assertEquals(24999.0, extractor.parsePrice("₹24,999")!!, 0.01)
        assertEquals(1499.50, extractor.parsePrice("Rs. 1,499.50")!!, 0.01)
        assertEquals(99.0, extractor.parsePrice("$99.00")!!, 0.01)
        assertEquals(3490.0, extractor.parsePrice("INR 3,490")!!, 0.01)
        assertEquals(499.0, extractor.parsePrice("499")!!, 0.01)
    }

    @Test
    fun `parseHtml extracts OpenGraph metadata and price`() {
        val sampleHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Sony WH-1000XM5 Headphones | Buy online at Tata CLiQ</title>
                <meta property="og:title" content="Sony WH-1000XM5 Wireless Headphones" />
                <meta property="og:image" content="https://img.tatacliq.com/sony.jpg" />
                <meta property="product:price:amount" content="26990.00" />
                <meta property="product:price:currency" content="INR" />
            </head>
            <body>
                <h1>Sony WH-1000XM5 Wireless Headphones</h1>
            </body>
            </html>
        """.trimIndent()

        val metadata = extractor.parseHtml(
            url = "https://www.tatacliq.com/p-12345",
            domain = "tatacliq.com",
            retailerName = "Tata CLiQ",
            html = sampleHtml
        )

        assertEquals("Sony WH-1000XM5 Wireless Headphones", metadata.title)
        assertEquals("https://img.tatacliq.com/sony.jpg", metadata.imageUrl)
        assertEquals(26990.0, metadata.price!!, 0.01)
        assertEquals("INR", metadata.currency)
        assertEquals("Tata CLiQ", metadata.retailerName)
    }

    @Test
    fun `parseHtml extracts Schema org JSON-LD price when meta tag missing`() {
        val sampleJsonLdHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Nike Air Max 90 Shoes</title>
                <script type="application/ld+json">
                {
                  "@context": "https://schema.org/",
                  "@type": "Product",
                  "name": "Nike Air Max 90",
                  "offers": {
                    "@type": "Offer",
                    "priceCurrency": "INR",
                    "price": 9995.00
                  }
                }
                </script>
            </head>
            <body>
                <h1>Nike Air Max 90</h1>
            </body>
            </html>
        """.trimIndent()

        val metadata = extractor.parseHtml(
            url = "https://www.nike.com/in/air-max-90",
            domain = "nike.com",
            retailerName = "Nike",
            html = sampleJsonLdHtml
        )

        assertEquals("Nike Air Max 90 Shoes", metadata.title)
        assertEquals(9995.0, metadata.price!!, 0.01)
    }

    @Test
    fun `parseHtml handles missing price and provides clean title from slug`() {
        val sampleEmptyHtml = """
            <!DOCTYPE html>
            <html>
            <head></head>
            <body>Loading SPA...</body>
            </html>
        """.trimIndent()

        val metadata = extractor.parseHtml(
            url = "https://www.customboutique.in/products/handcrafted-leather-wallet",
            domain = "customboutique.in",
            retailerName = "Customboutique",
            html = sampleEmptyHtml
        )

        assertTrue(metadata.title.contains("Handcrafted Leather Wallet"))
        assertNull(metadata.price)
    }
}

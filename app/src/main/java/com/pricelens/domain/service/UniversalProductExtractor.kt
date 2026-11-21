package com.pricelens.domain.service

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
data class UniversalProductMetadata(
    val url: String,
    val domain: String,
    val retailerName: String,
    val title: String,
    val price: Double?,
    val currency: String = "INR",
    val imageUrl: String? = null,
    val detectedSelector: String? = null
)

@Singleton
class UniversalProductExtractor @Inject constructor() {

    private val embeddedUrlRegex = Regex("""https?://[^\s<>"{}|\\^`]+""", RegexOption.IGNORE_CASE)

    suspend fun extractMetadata(rawUrl: String): UniversalProductMetadata = withContext(Dispatchers.IO) {
        val normalizedUrl = normalizeUrl(rawUrl)
        val domain = extractDomain(normalizedUrl)
        val retailerName = formatRetailerName(domain)

        try {
            val html = fetchHtml(normalizedUrl)
            parseHtml(normalizedUrl, domain, retailerName, html)
        } catch (e: Exception) {
            UniversalProductMetadata(
                url = normalizedUrl,
                domain = domain,
                retailerName = retailerName,
                title = extractTitleFromUrl(normalizedUrl, retailerName),
                price = null,
                currency = "INR",
                imageUrl = null
            )
        }
    }

    fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim()
        val extracted = embeddedUrlRegex.find(trimmed)?.value ?: trimmed
        return if (!extracted.startsWith("http://", ignoreCase = true) &&
            !extracted.startsWith("https://", ignoreCase = true)
        ) {
            "https://$extracted"
        } else {
            extracted
        }
    }

    fun extractDomain(url: String): String {
        return try {
            val uri = URI(url)
            val host = uri.host.orEmpty().lowercase()
            if (host.startsWith("www.")) host.substring(4) else host
        } catch (e: Exception) {
            "website"
        }
    }

    fun formatRetailerName(domain: String): String {
        val lower = domain.lowercase()
        return when {
            lower.contains("amazon") -> "Amazon India"
            lower.contains("flipkart") -> "Flipkart"
            lower.contains("myntra") -> "Myntra"
            lower.contains("tatacliq") -> "Tata CLiQ"
            lower.contains("croma") -> "Croma"
            lower.contains("reliancedigital") -> "Reliance Digital"
            lower.contains("vijaysales") -> "Vijay Sales"
            lower.contains("ajio") -> "Ajio"
            lower.contains("nykaa") -> "Nykaa"
            lower.contains("meesho") -> "Meesho"
            lower.contains("apple") -> "Apple"
            lower.contains("nike") -> "Nike"
            lower.contains("samsung") -> "Samsung"
            else -> {
                val clean = domain.split(".").firstOrNull() ?: domain
                clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
        }
    }

    fun parseHtml(url: String, domain: String, retailerName: String, html: String): UniversalProductMetadata {
        val title = extractMetaTag(html, "og:title")
            ?: extractMetaTag(html, "twitter:title")
            ?: extractTagContent(html, "title")
            ?: extractTitleFromUrl(url, retailerName)

        val cleanTitle = cleanTitle(title, retailerName)

        val imageUrl = extractMetaTag(html, "og:image")
            ?: extractMetaTag(html, "twitter:image")
            ?: extractLinkTag(html, "image_src")

        val currency = extractMetaTag(html, "product:price:currency")
            ?: extractMetaTag(html, "og:price:currency")
            ?: "INR"

        var price: Double? = null
        var selector: String? = null

        // 1. Try OpenGraph / Product Meta Price
        val metaPriceStr = extractMetaTag(html, "product:price:amount")
            ?: extractMetaTag(html, "og:price:amount")
        if (!metaPriceStr.isNullOrBlank()) {
            price = parsePrice(metaPriceStr)
            if (price != null) {
                selector = "meta[property='product:price:amount']"
            }
        }

        // 2. Try JSON-LD Schema.org Product
        if (price == null) {
            val jsonLdPrice = extractJsonLdPrice(html)
            if (jsonLdPrice != null) {
                price = jsonLdPrice
                selector = "script[type='application/ld+json']"
            }
        }

        // 3. Fallback: Common HTML Price Patterns
        if (price == null) {
            price = extractHeuristicPrice(html)
        }

        return UniversalProductMetadata(
            url = url,
            domain = domain,
            retailerName = retailerName,
            title = cleanTitle,
            price = price,
            currency = currency,
            imageUrl = imageUrl,
            detectedSelector = selector
        )
    }

    fun parsePrice(raw: String): Double? {
        val cleaned = raw.replace(",", "").trim()
        val regex = Regex("""(?:₹|Rs\.?|INR|\$|€|£)?\s*([\d]+(?:\.\d{1,2})?)""")
        val match = regex.find(cleaned)
        return match?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun extractJsonLdPrice(html: String): Double? {
        val scriptPattern = Pattern.compile(
            """<script[^>]*type=["']application/ld\+json["'][^>]*>(.*?)</script>""",
            Pattern.DOTALL or Pattern.CASE_INSENSITIVE
        )
        val matcher = scriptPattern.matcher(html)
        while (matcher.find()) {
            val json = matcher.group(1).orEmpty()
            if (json.contains("\"Product\"", ignoreCase = true) || json.contains("schema.org", ignoreCase = true)) {
                // Find price
                val priceRegex = Regex(""""(?:price|lowPrice)"\s*:\s*"?([\d.]+)"?""", RegexOption.IGNORE_CASE)
                val priceMatch = priceRegex.find(json)
                val value = priceMatch?.groupValues?.get(1)?.toDoubleOrNull()
                if (value != null && value > 0) {
                    return value
                }
            }
        }
        return null
    }

    private fun extractHeuristicPrice(html: String): Double? {
        // Looks for price classes/ids e.g. class="...price..." >₹1,999<
        val priceTagPattern = Pattern.compile(
            """(?:class|id)=["'][^"']*(?:price|offer|deal|cost)[^"']*["'][^>]*>[^<]*(?:₹|Rs\.?|INR)\s*([\d,]+(?:\.\d{1,2})?)""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = priceTagPattern.matcher(html)
        if (matcher.find()) {
            val found = matcher.group(1)
            if (!found.isNullOrBlank()) {
                val parsed = parsePrice(found)
                if (parsed != null && parsed > 0) return parsed
            }
        }

        // General INR pattern
        val inrPattern = Pattern.compile("""(?:₹|Rs\.?\s*)\s*([\d,]{2,10}(?:\.\d{1,2})?)""")
        val inrMatcher = inrPattern.matcher(html)
        if (inrMatcher.find()) {
            val found = inrMatcher.group(1)
            if (!found.isNullOrBlank()) {
                val parsed = parsePrice(found)
                if (parsed != null && parsed > 0) return parsed
            }
        }

        return null
    }

    private fun extractMetaTag(html: String, property: String): String? {
        val regex = Regex(
            """<meta[^>]*(?:property|name)=["']$property["'][^>]*content=["'](.*?)["']""",
            RegexOption.IGNORE_CASE
        )
        val match = regex.find(html)
        if (match != null) {
            return decodeHtmlEntities(match.groupValues[1].trim())
        }
        // Try content before property/name
        val altRegex = Regex(
            """<meta[^>]*content=["'](.*?)["'][^>]*(?:property|name)=["']$property["']""",
            RegexOption.IGNORE_CASE
        )
        return altRegex.find(html)?.groupValues?.get(1)?.trim()?.let { decodeHtmlEntities(it) }
    }

    private fun extractLinkTag(html: String, rel: String): String? {
        val regex = Regex("""<link[^>]*rel=["']$rel["'][^>]*href=["'](.*?)["']""", RegexOption.IGNORE_CASE)
        return regex.find(html)?.groupValues?.get(1)?.trim()
    }

    private fun extractTagContent(html: String, tag: String): String? {
        val regex = Regex("""<$tag[^>]*>(.*?)</$tag>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        return regex.find(html)?.groupValues?.get(1)?.trim()?.let { decodeHtmlEntities(it) }
    }

    private fun cleanTitle(title: String, retailerName: String): String {
        return title
            .replace(Regex("""\s*[\|\-–—:]\s*(?:Buy online at|Shop online|Online in India|Amazon\.in|Flipkart|Tata CLiQ|Myntra|Croma|Vijay Sales|Reliance Digital).*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*[\|\-–—]\s*$retailerName.*$""", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun extractTitleFromUrl(url: String, retailerName: String): String {
        return try {
            val uri = URI(url)
            val path = uri.path.orEmpty().trim('/')
            val lastSegment = path.split('/').lastOrNull { it.isNotBlank() && !it.startsWith("p-") && !it.startsWith("dp") }
            if (!lastSegment.isNullOrBlank()) {
                lastSegment.replace("-", " ").replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            } else {
                "Tracked Product ($retailerName)"
            }
        } catch (e: Exception) {
            "Tracked Product ($retailerName)"
        }
    }

    private fun decodeHtmlEntities(input: String): String {
        return input
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&#8377;", "₹")
            .replace("&nbsp;", " ")
    }

    private fun fetchHtml(urlStr: String): String {
        var currentUrl = urlStr
        var redirects = 0
        while (redirects < 5) {
            val url = URL(currentUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.instanceFollowRedirects = true
            conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
            )
            conn.setRequestProperty(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
            )
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9")

            val status = conn.responseCode
            if (status in 300..399) {
                val location = conn.getHeaderField("Location")
                if (!location.isNullOrBlank()) {
                    currentUrl = if (location.startsWith("http")) location else URL(url, location).toString()
                    redirects++
                    continue
                }
            }

            if (status in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                var bytesRead = 0
                val maxBytes = 400 * 1024 // 400 KB limit
                while (reader.readLine().also { line = it } != null) {
                    sb.appendLine(line)
                    bytesRead += (line?.length ?: 0)
                    if (bytesRead >= maxBytes) break
                }
                reader.close()
                return sb.toString()
            } else {
                throw IllegalStateException("HTTP $status")
            }
        }
        throw IllegalStateException("Too many redirects")
    }
}

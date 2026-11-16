package com.pricelens.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pricelens.core.designsystem.PriceLensTheme

/**
 * Resolves a reliable product image URL, using semantic category matching when the URL is missing.
 */
fun resolveProductImageUrl(url: String?, productName: String): String {
    if (!url.isNullOrBlank()) return url

    val lower = productName.lowercase()
    return when {
        lower.contains("iphone") || lower.contains("apple") || lower.contains("ipad") ->
            "https://images.unsplash.com/photo-1592750475338-74b7b21085ab"
        lower.contains("macbook") || lower.contains("laptop") || lower.contains("notebook") ->
            "https://images.unsplash.com/photo-1517336714731-489689fd1ca8"
        lower.contains("airpod") || lower.contains("headphone") || lower.contains("boat") || lower.contains("audio") || lower.contains("buds") ->
            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e"
        lower.contains("samsung") || lower.contains("galaxy") ->
            "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c"
        lower.contains("sony") || lower.contains("ps5") || lower.contains("playstation") ->
            "https://images.unsplash.com/photo-1606813907291-d86efa9b94db"
        lower.contains("watch") || lower.contains("smartwatch") ->
            "https://images.unsplash.com/photo-1523275335684-37898b6baf30"
        lower.contains("shoe") || lower.contains("sneaker") || lower.contains("nike") || lower.contains("puma") || lower.contains("adidas") ->
            "https://images.unsplash.com/photo-1542291026-7eec264c27ff"
        else ->
            "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f"
    }
}

@Composable
fun ProductThumbnailImage(
    imageUrl: String?,
    productName: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val context = LocalContext.current
    val resolvedUrl = remember(imageUrl, productName) {
        resolveProductImageUrl(imageUrl, productName)
    }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    val isVisible = !isLoading && !hasError
    val imageAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "thumbAlpha"
    )
    val imageScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.93f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "thumbScale"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (isLoading) Modifier.background(shimmerBrush(), shape)
                else Modifier.background(PriceLensTheme.glassColors.surfaceHighlight, shape)
            )
            .border(0.5.dp, PriceLensTheme.glassColors.specularBorder, shape),
        contentAlignment = Alignment.Center
    ) {
        if (!hasError) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resolvedUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = productName,
                contentScale = ContentScale.Crop,
                onLoading = { isLoading = true; hasError = false },
                onSuccess = { isLoading = false; hasError = false },
                onError = { isLoading = false; hasError = true },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = imageAlpha
                        scaleX = imageScale
                        scaleY = imageScale
                    }
            )
        }

        if (hasError) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = PriceLensTheme.glassColors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ProductHeroImage(
    imageUrl: String?,
    productName: String,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 200.dp
) {
    val context = LocalContext.current
    val resolvedUrl = remember(imageUrl, productName) {
        resolveProductImageUrl(imageUrl, productName)
    }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    val isVisible = !isLoading && !hasError
    val heroAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "heroAlpha"
    )
    val heroScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.94f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow),
        label = "heroScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isLoading) Modifier.background(shimmerBrush(), RoundedCornerShape(16.dp))
                else Modifier.background(PriceLensTheme.glassColors.surfaceHighlight.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            )
            .border(1.dp, PriceLensTheme.glassColors.specularBorder, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!hasError) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resolvedUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = productName,
                contentScale = ContentScale.Fit,
                onLoading = { isLoading = true; hasError = false },
                onSuccess = { isLoading = false; hasError = false },
                onError = { isLoading = false; hasError = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .graphicsLayer {
                        alpha = heroAlpha
                        scaleX = heroScale
                        scaleY = heroScale
                    }
            )
        }

        if (hasError) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = PriceLensTheme.glassColors.textTertiary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

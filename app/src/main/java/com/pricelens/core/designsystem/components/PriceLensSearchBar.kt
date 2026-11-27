package com.pricelens.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.pricelens.core.designsystem.PriceLensTheme

@Composable
fun PriceLensSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search products, brands, or models…",
    onClear: () -> Unit = { onQueryChange("") }
) {
    val isCupertino = PriceLensTheme.isCupertino
    val isDark = PriceLensTheme.isDark
    val canvasBackdrop = PriceLensTheme.canvasBackdrop
    val effectIntensity = if (isCupertino) PriceLensTheme.iosGlassIntensity else 1.0f
    val surfaceGlass = PriceLensTheme.glassColors.surfaceGlass
    val activeBackdrop = if (isCupertino) canvasBackdrop else null
    val useBackdrop = activeBackdrop != null

    val glassModifier = if (activeBackdrop != null) {
        Modifier
            .drawBackdrop(
                backdrop = activeBackdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur((8f * effectIntensity).dp.toPx())
                    lens(
                        refractionHeight = (8f * effectIntensity).dp.toPx(),
                        refractionAmount = (12f * effectIntensity).dp.toPx(),
                        depthEffect = true,
                        chromaticAberration = true
                    )
                },
                highlight = {
                    Highlight.Default.copy(
                        width = 0.75.dp,
                        alpha = if (isDark) 0.55f else 0.78f
                    )
                },
                shadow = {
                    Shadow(
                        radius = 10.dp,
                        color = Color.Black.copy(alpha = if (isDark) 0.22f else 0.06f)
                    )
                },
                innerShadow = {
                    InnerShadow(
                        radius = (4f * effectIntensity).dp,
                        offset = DpOffset(0.dp, 1.5.dp),
                        color = Color.White.copy(alpha = if (isDark) 0.10f else 0.42f),
                        alpha = 0.72f
                    )
                },
                onDrawSurface = { drawRect(surfaceGlass) }
            )
            .clip(Capsule())
            .border(0.75.dp, PriceLensTheme.glassColors.specularBorderBrush, Capsule())
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .then(glassModifier)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PriceLensTheme.glassColors.textTertiary
                )
            },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(PriceLensTheme.glassColors.ambientGlow.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = PriceLensTheme.glassColors.ambientGlow,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                    exit = fadeOut() + scaleOut()
                ) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = PriceLensTheme.glassColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
            shape = RoundedCornerShape(9999.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (useBackdrop) Color.Transparent else surfaceGlass,
                unfocusedContainerColor = if (useBackdrop) Color.Transparent else surfaceGlass,
                focusedBorderColor = if (useBackdrop) Color.Transparent else PriceLensTheme.glassColors.ambientGlow,
                unfocusedBorderColor = if (useBackdrop) Color.Transparent else PriceLensTheme.glassColors.specularBorder,
                focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                unfocusedTextColor = PriceLensTheme.glassColors.textPrimary,
                cursorColor = PriceLensTheme.glassColors.ambientGlow
            )
        )
    }
}

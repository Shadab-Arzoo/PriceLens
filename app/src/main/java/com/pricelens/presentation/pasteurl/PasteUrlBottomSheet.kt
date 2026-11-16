package com.pricelens.presentation.pasteurl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pricelens.core.designsystem.AmberAlert
import com.pricelens.core.designsystem.BrandMint
import com.pricelens.core.designsystem.CoralError
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.LiquidGlassPrimaryButton
import com.pricelens.domain.model.ConfirmedProductIdentity
import com.pricelens.domain.service.UniversalProductMetadata
import com.pricelens.domain.usecase.UrlValidationResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PasteUrlBottomSheet(
    urlInput: String,
    validationResult: UrlValidationResult?,
    confirmedIdentity: ConfirmedProductIdentity? = null,
    extractedMetadata: UniversalProductMetadata? = null,
    isExtracting: Boolean = false,
    onUrlChange: (String) -> Unit,
    onIdentityChange: (name: String, brand: String?, model: String?) -> Unit = { _, _, _ -> },
    onSubmitConfirmedUrl: (ConfirmedProductIdentity) -> Unit = {},
    onSubmitUrl: () -> Unit,
    onTrackUniversal: (UniversalProductMetadata, Double) -> Unit = { _, _ -> },
    onOpenWebPicker: (url: String, name: String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    var universalTargetPrice by remember(extractedMetadata) {
        mutableStateOf(
            if (extractedMetadata?.price != null && extractedMetadata.price > 0) {
                "%.0f".format(extractedMetadata.price * 0.95)
            } else {
                ""
            }
        )
    }

    val isDark = PriceLensTheme.isDark
    val glow = PriceLensTheme.glassColors.ambientGlow
    val iconBg = if (isDark) BrandMint.copy(alpha = 0.14f) else Color(0xFFE6F4EA)
    val iconBorder = if (isDark) BrandMint.copy(alpha = 0.35f) else Color(0xFFCEEAD6)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PriceLensTheme.glassColors.surfaceGlass,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .animateContentSize(animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconBg)
                        .border(1.dp, iconBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Universal Price Tracker",
                        tint = glow,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Track Any Product Link",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    Text(
                        text = "Paste link from any website or e-commerce store",
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Text field with Paste button
            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Paste any product link (Amazon, Flipkart, Tata CLiQ, Myntra…)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PriceLensTheme.glassColors.textTertiary
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val text = clipboardManager.getText()?.text
                            if (!text.isNullOrBlank()) {
                                onUrlChange(text.trim())
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste from Clipboard",
                            tint = glow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(9999.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = glow,
                    unfocusedBorderColor = PriceLensTheme.glassColors.specularBorder,
                    focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                    unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Loading state while auto-extracting metadata
            AnimatedVisibility(
                visible = isExtracting,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (isDark) BrandMint.copy(alpha = 0.4f) else Color(0xFFCEEAD6)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = glow,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Analyzing webpage & detecting live prices…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Universal Product Preview Card (if extracted)
            AnimatedVisibility(
                visible = extractedMetadata != null,
                enter = fadeIn() + expandVertically(spring(dampingRatio = 0.8f, stiffness = 380f)),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (extractedMetadata != null) {
                    Column {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = if (isDark) BrandMint.copy(alpha = 0.6f) else Color(0xFFCEEAD6)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(iconBg)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = extractedMetadata.retailerName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = glow
                                        )
                                    }

                                    Text(
                                        text = extractedMetadata.domain,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    com.pricelens.core.designsystem.components.ProductThumbnailImage(
                                        imageUrl = extractedMetadata.imageUrl,
                                        productName = extractedMetadata.title,
                                        modifier = Modifier.size(52.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = extractedMetadata.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PriceLensTheme.glassColors.textPrimary,
                                        maxLines = 2,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (extractedMetadata.price != null && extractedMetadata.price > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Live Detected Price",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PriceLensTheme.glassColors.textSecondary
                                            )
                                            Text(
                                                text = "₹${"%,.0f".format(extractedMetadata.price)}",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = glow
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = universalTargetPrice,
                                        onValueChange = { universalTargetPrice = it },
                                        label = { Text("Alert me when price drops below (₹)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                                            unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                onOpenWebPicker(extractedMetadata.url, extractedMetadata.title)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(9999.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.GpsFixed,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = PriceLensTheme.glassColors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Select on Page",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = PriceLensTheme.glassColors.textPrimary
                                            )
                                        }

                                        LiquidGlassPrimaryButton(
                                            text = "Track Price",
                                            onClick = {
                                                val targetDouble = universalTargetPrice.toDoubleOrNull() ?: extractedMetadata.price
                                                onTrackUniversal(extractedMetadata, targetDouble)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                } else {
                                    // Price wasn't found automatically
                                    Text(
                                        text = "Price couldn't be auto-detected. Tap below to open the webpage and select the price spot directly.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LiquidGlassPrimaryButton(
                                        text = "Open Web Page & Select Price",
                                        onClick = {
                                            onOpenWebPicker(extractedMetadata.url, extractedMetadata.title)
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Catalog Comparison Fallback for Supported Retailers
            when (validationResult) {
                is UrlValidationResult.Supported -> {
                    val identity = confirmedIdentity ?: validationResult.suggestedIdentity
                    if (identity != null) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = ElectricBlue.copy(alpha = 0.4f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Catalog Multi-Store Comparison",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                                Text(
                                    text = "Compare ${validationResult.retailerName} against all authorized Indian retailers:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        onSubmitConfirmedUrl(identity.confirm())
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(9999.dp)
                                ) {
                                    Text("Compare Partner Stores")
                                }
                            }
                        }
                    }
                }
                is UrlValidationResult.UnsupportedRetailer -> {
                    if (extractedMetadata == null && !isExtracting) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = if (isDark) BrandMint.copy(alpha = 0.08f) else Color(0xFFE6F4EA).copy(alpha = 0.5f),
                            borderColor = if (isDark) BrandMint.copy(alpha = 0.35f) else Color(0xFFCEEAD6)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = glow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Universal Website: ${validationResult.domain}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PriceLensTheme.glassColors.textPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "You can track prices on this website! Tap below to open the webpage and select the price spot.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LiquidGlassPrimaryButton(
                                    text = "Open Web Page & Select Price",
                                    onClick = {
                                        onOpenWebPicker(urlInput, "")
                                    }
                                )
                            }
                        }
                    }
                }
                is UrlValidationResult.InvalidUrl -> {
                    if (urlInput.isNotBlank()) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = CoralError.copy(alpha = 0.12f),
                            borderColor = CoralError.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = CoralError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = validationResult.reason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                            }
                        }
                    }
                }
                null -> {
                    // Empty state hint
                    Column {
                        Text(
                            text = "Universal Web Tracking",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Paste any product URL from any site. PriceLens automatically detects the product and lets you inspect and track any price spot on the page.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

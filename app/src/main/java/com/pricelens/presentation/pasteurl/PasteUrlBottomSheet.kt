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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pricelens.core.designsystem.AmberAlert
import com.pricelens.core.designsystem.CoralError
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.LiquidGlassPrimaryButton
import com.pricelens.domain.model.ConfirmedProductIdentity
import com.pricelens.domain.usecase.UrlValidationResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PasteUrlBottomSheet(
    urlInput: String,
    validationResult: UrlValidationResult?,
    confirmedIdentity: ConfirmedProductIdentity? = null,
    onUrlChange: (String) -> Unit,
    onIdentityChange: (name: String, brand: String?, model: String?) -> Unit = { _, _, _ -> },
    onSubmitConfirmedUrl: (ConfirmedProductIdentity) -> Unit = {},
    onSubmitUrl: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current

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
                        .clip(RoundedCornerShape(13.dp))
                        .background(ElectricBlue.copy(alpha = 0.15f))
                        .border(1.dp, ElectricBlue.copy(alpha = 0.35f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste Product Link",
                        tint = ElectricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Paste Product Link",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    Text(
                        text = "Real-time comparison across authorized Indian stores",
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
                        text = "https://www.amazon.in/dp/… or flipkart.com/…",
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
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = PriceLensTheme.glassColors.specularBorder,
                    focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                    unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Validation Status Feedback
            when (validationResult) {
                is UrlValidationResult.Supported -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MintSuccess.copy(alpha = 0.12f),
                            borderColor = MintSuccess.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MintSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Supported retailer: ${validationResult.retailerName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                            }
                        }

                        val identity = confirmedIdentity ?: validationResult.suggestedIdentity
                        if (identity != null) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = ElectricBlue.copy(alpha = 0.4f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Confirm Product Identity",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PriceLensTheme.glassColors.textPrimary
                                    )
                                    Text(
                                        text = "Verify or customize the identified product model:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PriceLensTheme.glassColors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = identity.editedName,
                                        onValueChange = { newName ->
                                            onIdentityChange(newName, identity.editedBrand, identity.editedModel)
                                        },
                                        label = { Text("Product Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                                            unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                                        ),
                                        singleLine = false,
                                        maxLines = 3
                                    )

                                    if (!identity.suggestedBrand.isNullOrBlank() || identity.editedBrand != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = identity.editedBrand.orEmpty(),
                                            onValueChange = { newBrand ->
                                                onIdentityChange(identity.editedName, newBrand, identity.editedModel)
                                            },
                                            label = { Text("Brand") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                                                unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                                            ),
                                            singleLine = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is UrlValidationResult.UnsupportedRetailer -> {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = AmberAlert.copy(alpha = 0.12f),
                        borderColor = AmberAlert.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AmberAlert,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Unsupported retailer: ${validationResult.domain}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PriceLensTheme.glassColors.textPrimary
                                )
                                Text(
                                    text = "PriceLens compares authorized partner feeds (Amazon, Flipkart, Croma, Reliance Digital, Vijay Sales).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PriceLensTheme.glassColors.textSecondary
                                )
                            }
                        }
                    }
                }
                is UrlValidationResult.InvalidUrl -> {
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
                null -> {
                    // Supported retailers badge list
                    Column {
                        Text(
                            text = "Supported Stores",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PriceLensTheme.glassColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Amazon.in", "Flipkart", "Croma", "Reliance Digital", "Vijay Sales").forEach { name ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PriceLensTheme.glassColors.surfaceHighlight)
                                        .border(0.5.dp, PriceLensTheme.glassColors.specularBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PriceLensTheme.glassColors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LiquidGlassPrimaryButton(
                text = "Confirm & Compare",
                enabled = validationResult is UrlValidationResult.Supported,
                onClick = {
                    val identity = confirmedIdentity ?: (validationResult as? UrlValidationResult.Supported)?.suggestedIdentity
                    if (identity != null) {
                        onSubmitConfirmedUrl(identity.confirm())
                    } else {
                        onSubmitUrl()
                    }
                }
            )
        }
    }
}

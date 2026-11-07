package com.pricelens.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pricelens.core.designsystem.AmberAlert
import com.pricelens.core.designsystem.CyanAccent
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.domain.model.ConfirmedProductIdentity

@Composable
fun ScannerRoute(
    viewModel: ScannerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToComparison: (productId: String, productName: String) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onEvent(ScannerUiEvent.CameraPermissionGranted)
        } else {
            viewModel.onEvent(ScannerUiEvent.CameraPermissionDenied)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(ScannerUiEvent.ImageSelected(it.toString()))
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            viewModel.onEvent(ScannerUiEvent.CameraPermissionGranted)
        }
    }

    ScannerScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        onPickImage = { galleryLauncher.launch("image/*") },
        onNavigateBack = onNavigateBack,
        onConfirmed = { identity ->
            if (!identity.resolvedProductId.isNullOrBlank()) {
                onNavigateToComparison(identity.resolvedProductId, identity.effectiveTitle)
            } else {
                onNavigateToSearch(identity.effectiveTitle)
            }
        },
        modifier = modifier
    )
}

@Composable
fun ScannerScreen(
    uiState: ScannerUiState,
    onEvent: (ScannerUiEvent) -> Unit,
    onRequestPermission: () -> Unit = {},
    onPickImage: () -> Unit = {},
    onNavigateBack: () -> Unit,
    onConfirmed: (ConfirmedProductIdentity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = PriceLensTheme.glassColors.backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = PriceLensTheme.glassColors.surfaceHighlight,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PriceLensTheme.glassColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Scan Product / OCR",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    Text(
                        text = "Camera barcode & label OCR with explicit confirmation",
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Permission Denial State
            if (uiState.isPermissionDenied && !uiState.hasCameraPermission) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = AmberAlert.copy(alpha = 0.12f),
                    borderColor = AmberAlert.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AmberAlert,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Camera Permission Denied",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Camera permission was denied. PriceLens needs camera access to scan barcodes and product labels in real-time. You can grant access or select a photo from your gallery instead.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onRequestPermission,
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Grant Permission")
                            }

                            OutlinedButton(
                                onClick = onPickImage,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gallery")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Viewfinder or Gallery Image Area
            if (uiState.ocrSuggestion == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Viewfinder Reticle Frame
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .border(
                                width = 2.dp,
                                color = CyanAccent,
                                shape = RoundedCornerShape(20.dp)
                            )
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (uiState.hasCameraPermission) "Align barcode or label within frame" else "Select image or enable camera",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "OCR extracts model identifiers for verified comparison",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions Row: Pick Image from Gallery or Sample Scans
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPickImage,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PriceLensTheme.glassColors.surfaceHighlight,
                            contentColor = PriceLensTheme.glassColors.textPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Image")
                    }

                    if (!uiState.hasCameraPermission && !uiState.isPermissionDenied) {
                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enable Camera")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Sample Scans for Testing and Verification
                Text(
                    text = "Quick Sample Label Tests:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PriceLensTheme.glassColors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SampleChip(
                        label = "Sony XM5 Label",
                        onClick = {
                            onEvent(ScannerUiEvent.ProcessOcrText("Sony WH-1000XM5 Wireless Noise Cancelling Headphones Model WH-1000XM5/B"))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SampleChip(
                        label = "iPhone 15 Box",
                        onClick = {
                            onEvent(ScannerUiEvent.ProcessOcrText("Apple iPhone 15 128GB Black Model A3090 MTP03HN/A"))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SampleChip(
                        label = "Galaxy S24 Barcode",
                        onClick = {
                            onEvent(ScannerUiEvent.ProcessOcrText("Samsung Galaxy S24 5G 128GB SM-S921BZKDINS"))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // OCR Processing Progress
            if (uiState.isProcessingOcr) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ElectricBlue,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Analyzing image and extracting text via OCR...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }
            }

            // OCR Error State
            if (uiState.errorMessage != null && !uiState.isProcessingOcr) {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = AmberAlert.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AmberAlert)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PriceLensTheme.glassColors.textPrimary
                        )
                    }
                }
            }

            // Editable Confirmed Product Identity Card
            if (uiState.ocrSuggestion != null) {
                Spacer(modifier = Modifier.height(16.dp))
                val suggestion = uiState.ocrSuggestion!!

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ElectricBlue.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MintSuccess.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MintSuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "OCR Identification Suggestion",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MintSuccess
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onEvent(ScannerUiEvent.ResetScan) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Rescan",
                                    tint = PriceLensTheme.glassColors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Please verify and edit identification before comparison:",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceLensTheme.glassColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Editable Product Title
                        OutlinedTextField(
                            value = suggestion.editedName,
                            onValueChange = { newName ->
                                onEvent(ScannerUiEvent.OcrSuggestionEdited(newName, suggestion.editedBrand, suggestion.editedModel))
                            },
                            label = { Text("Product Title (Editable)") },
                            trailingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                                unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                            ),
                            singleLine = false,
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Editable Brand
                        OutlinedTextField(
                            value = suggestion.editedBrand.orEmpty(),
                            onValueChange = { newBrand ->
                                onEvent(ScannerUiEvent.OcrSuggestionEdited(suggestion.editedName, newBrand, suggestion.editedModel))
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

                        Spacer(modifier = Modifier.height(8.dp))

                        // Editable Model Number
                        OutlinedTextField(
                            value = suggestion.editedModel.orEmpty(),
                            onValueChange = { newModel ->
                                onEvent(ScannerUiEvent.OcrSuggestionEdited(suggestion.editedName, suggestion.editedBrand, newModel))
                            },
                            label = { Text("Model Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                                unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                            ),
                            singleLine = true
                        )

                        if (!uiState.rawOcrText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Raw OCR text: \"${uiState.rawOcrText}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = PriceLensTheme.glassColors.textTertiary,
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Confirm & Compare Button
                        Button(
                            onClick = {
                                val confirmed = suggestion.confirm()
                                onEvent(ScannerUiEvent.ConfirmProduct(confirmed))
                                onConfirmed(confirmed)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Confirm & Compare Product",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SampleChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = PriceLensTheme.glassColors.surfaceHighlight,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = PriceLensTheme.glassColors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
        )
    }
}

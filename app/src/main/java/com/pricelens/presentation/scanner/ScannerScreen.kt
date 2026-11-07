package com.pricelens.presentation.scanner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pricelens.R
import com.pricelens.core.designsystem.AmberAlert
import com.pricelens.core.designsystem.CyanAccent
import com.pricelens.core.designsystem.CyanGlow
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.LiquidGlassPrimaryButton
import com.pricelens.core.designsystem.components.LiquidGlassSecondaryButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ScannerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToComparison: (productId: String, productName: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isTorchEnabled by remember { mutableStateOf(false) }
    var detectedProductFeedback by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Re-check permission when resuming app from Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Simulated photo barcode detection
            coroutineScope.launch {
                detectedProductFeedback = "Photo barcode recognized: Sony WH-1000XM5"
                delay(1200)
                onNavigateToComparison("prod-sony-wh1000xm5", "Sony WH-1000XM5")
            }
        }
    }

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
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PriceLensTheme.glassColors.surfaceHighlight)
                        .border(1.dp, PriceLensTheme.glassColors.specularBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PriceLensTheme.glassColors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.scanner_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PriceLensTheme.glassColors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.scanner_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceLensTheme.glassColors.textSecondary
                    )
                }

                if (hasCameraPermission) {
                    IconButton(
                        onClick = { isTorchEnabled = !isTorchEnabled },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isTorchEnabled) CyanAccent.copy(alpha = 0.25f)
                                else PriceLensTheme.glassColors.surfaceHighlight
                            )
                            .border(
                                1.dp,
                                if (isTorchEnabled) CyanAccent else PriceLensTheme.glassColors.specularBorder,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = stringResource(R.string.scanner_torch),
                            tint = if (isTorchEnabled) CyanAccent else PriceLensTheme.glassColors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!hasCameraPermission) {
                // Permission Required Screen with Liquid Glass Aesthetics
                PermissionRequestView(
                    onRequestPermission = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onOpenSettings = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    }
                )
            } else {
                // Camera Viewfinder & Barcode Scanner
                ActiveScannerView(
                    isTorchEnabled = isTorchEnabled,
                    detectedProductFeedback = detectedProductFeedback,
                    onPickPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onSelectSampleBarcode = { productId, productName, barcode ->
                        coroutineScope.launch {
                            detectedProductFeedback = "Barcode match: $barcode ($productName)"
                            delay(900)
                            onNavigateToComparison(productId, productName)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Liquid Glass Camera Permission Request View
 */
@Composable
private fun PermissionRequestView(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Floating Hero Camera Glass Card
        GlassCard(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(32.dp),
            elevation = 6.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.camera_permission_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PriceLensTheme.glassColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.camera_permission_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = PriceLensTheme.glassColors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feature rationale cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PermissionBenefitRow(
                icon = Icons.Default.QrCodeScanner,
                text = stringResource(R.string.camera_permission_feature_1)
            )
            PermissionBenefitRow(
                icon = Icons.Default.CameraAlt,
                text = stringResource(R.string.camera_permission_feature_2)
            )
            PermissionBenefitRow(
                icon = Icons.Default.Security,
                text = stringResource(R.string.camera_permission_feature_3)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        LiquidGlassPrimaryButton(
            text = stringResource(R.string.grant_camera_permission),
            icon = Icons.Default.CameraAlt,
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LiquidGlassSecondaryButton(
            text = stringResource(R.string.permission_denied_settings),
            icon = Icons.Default.Settings,
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PermissionBenefitRow(
    icon: ImageVector,
    text: String
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = PriceLensTheme.glassColors.textPrimary
            )
        }
    }
}

/**
 * Active Camera Viewfinder with animated laser scan beam & quick barcode inputs
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveScannerView(
    isTorchEnabled: Boolean,
    detectedProductFeedback: String?,
    onPickPhoto: () -> Unit,
    onSelectSampleBarcode: (productId: String, productName: String, barcode: String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scannerLaser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserSweep"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Viewfinder Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.90f))
                .border(1.5.dp, PriceLensTheme.glassColors.specularBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Viewfinder Reticle Frame
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .border(2.dp, CyanAccent, RoundedCornerShape(20.dp))
            )

            // Animated Laser Scan Beam with Cyan Glow
            Box(
                modifier = Modifier
                    .width(216.dp)
                    .height(3.dp)
                    .offset(y = ((laserY - 0.5f) * 190).dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                CyanGlow,
                                Color.White,
                                CyanGlow,
                                Color.Transparent
                            )
                        )
                    )
                    .shadow(elevation = 8.dp, spotColor = CyanGlow)
            )

            // Live status badge
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isTorchEnabled) AmberAlert else CyanAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTorchEnabled) "Camera Active • Torch On" else "Scanning retail barcode…",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }

            // Detection feedback banner
            if (detectedProductFeedback != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MintSuccess.copy(alpha = 0.95f))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = detectedProductFeedback,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Action: Choose Photo from Gallery
        LiquidGlassSecondaryButton(
            text = stringResource(R.string.scanner_gallery),
            icon = Icons.Default.PhotoLibrary,
            onClick = onPickPhoto,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Try Sample Barcodes Section
        Text(
            text = stringResource(R.string.scanner_sample_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = PriceLensTheme.glassColors.textPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SampleBarcodeChip(
                label = "iPhone 16 Pro",
                code = "8901234567890",
                onClick = {
                    onSelectSampleBarcode("prod-iphone16-128", "Apple iPhone 16 Pro", "8901234567890")
                }
            )
            SampleBarcodeChip(
                label = "Sony WH-1000XM5",
                code = "8901234567891",
                onClick = {
                    onSelectSampleBarcode("prod-sony-wh1000xm5", "Sony WH-1000XM5", "8901234567891")
                }
            )
            SampleBarcodeChip(
                label = "Galaxy S24",
                code = "8901234567892",
                onClick = {
                    onSelectSampleBarcode("prod-samsung-s24", "Samsung Galaxy S24", "8901234567892")
                }
            )
            SampleBarcodeChip(
                label = "MacBook Air M3",
                code = "8901234567893",
                onClick = {
                    onSelectSampleBarcode("prod-macbook-air-m3", "MacBook Air M3", "8901234567893")
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SampleBarcodeChip(
    label: String,
    code: String,
    onClick: () -> Unit
) {
    GlassCard(
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PriceLensTheme.glassColors.textPrimary
                )
                Text(
                    text = code,
                    style = MaterialTheme.typography.labelSmall,
                    color = PriceLensTheme.glassColors.textSecondary
                )
            }
        }
    }
}

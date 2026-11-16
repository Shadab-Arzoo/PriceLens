package com.pricelens.presentation.webpicker

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pricelens.core.designsystem.BrandMint
import com.pricelens.core.designsystem.ElectricBlue
import com.pricelens.core.designsystem.MintSuccess
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.designsystem.components.GlassCard
import com.pricelens.core.designsystem.components.LiquidGlassPrimaryButton

private const val INSPECTOR_JS = """
(function() {
    if (window.priceLensInspector) return;
    
    window.priceLensInspector = {
        mode: 'inspect',
        selectedEl: null,
        
        setMode: function(m) {
            this.mode = m;
            if (m === 'browse') {
                this.clearHighlight();
            }
        },
        
        highlight: function(el) {
            if (this.selectedEl && this.selectedEl !== el) {
                this.selectedEl.style.outline = '';
                this.selectedEl.style.outlineOffset = '';
                this.selectedEl.style.backgroundColor = '';
            }
            this.selectedEl = el;
            el.style.outline = '3px solid #6EE3AD';
            el.style.outlineOffset = '2px';
            el.style.backgroundColor = 'rgba(110, 227, 173, 0.22)';
        },
        
        clearHighlight: function() {
            if (this.selectedEl) {
                this.selectedEl.style.outline = '';
                this.selectedEl.style.outlineOffset = '';
                this.selectedEl.style.backgroundColor = '';
                this.selectedEl = null;
            }
        },
        
        getSelector: function(el) {
            if (!el) return '';
            if (el.id) return '#' + el.id;
            var path = [];
            while (el && el.nodeType === Node.ELEMENT_NODE) {
                var selector = el.nodeName.toLowerCase();
                if (el.id) {
                    selector += '#' + el.id;
                    path.unshift(selector);
                    break;
                } else {
                    var sibling = el, nth = 1;
                    while (sibling = sibling.previousElementSibling) {
                        if (sibling.nodeName.toLowerCase() == selector) nth++;
                    }
                    if (nth != 1) selector += ":nth-of-type("+nth+")";
                }
                path.unshift(selector);
                el = el.parentNode;
            }
            return path.join(" > ");
        },
        
        handleSelect: function(el) {
            this.highlight(el);
            var text = el.innerText || el.textContent || '';
            var selector = this.getSelector(el);
            var pageTitle = document.title || '';
            var pageUrl = window.location.href;
            if (window.PriceLensBridge) {
                window.PriceLensBridge.onElementSelected(text.trim(), selector, pageTitle, pageUrl);
            }
        },
        
        init: function() {
            var self = this;
            document.addEventListener('click', function(e) {
                if (self.mode === 'inspect') {
                    e.preventDefault();
                    e.stopPropagation();
                    self.handleSelect(e.target);
                    return false;
                }
            }, true);
            
            document.addEventListener('touchstart', function(e) {
                if (self.mode === 'inspect' && e.touches.length === 1) {
                    var touch = e.touches[0];
                    var el = document.elementFromPoint(touch.clientX, touch.clientY);
                    if (el) {
                        self.highlight(el);
                    }
                }
            }, { passive: true });
        }
    };
    
    window.priceLensInspector.init();
})();
"""

class PriceLensWebInterface(
    private val onSelected: (rawText: String, selector: String, title: String, url: String) -> Unit
) {
    @JavascriptInterface
    fun onElementSelected(rawText: String, selector: String, title: String, url: String) {
        onSelected(rawText, selector, title, url)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPricePickerScreen(
    viewModel: WebPricePickerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.isSavedSuccessfully) {
        if (uiState.isSavedSuccessfully) {
            onNavigateBack()
        }
    }

    // Toggle inspector mode inside WebView whenever mode state changes
    LaunchedEffect(uiState.isInspectMode) {
        val modeStr = if (uiState.isInspectMode) "inspect" else "browse"
        webViewInstance?.evaluateJavascript("if (window.priceLensInspector) { window.priceLensInspector.setMode('$modeStr'); }", null)
    }

    val isDark = PriceLensTheme.isDark
    val glow = PriceLensTheme.glassColors.ambientGlow

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PriceLensTheme.glassColors.backgroundBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PriceLensTheme.glassColors.surfaceGlass,
                border = BorderStroke(1.dp, PriceLensTheme.glassColors.specularBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close",
                                tint = PriceLensTheme.glassColors.textPrimary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.retailerName.ifBlank { uiState.domain.ifBlank { "PriceLens Universal Picker" } },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = uiState.url,
                                style = MaterialTheme.typography.labelSmall,
                                color = PriceLensTheme.glassColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Mode Selector Toggle Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(PriceLensTheme.glassColors.surfaceHighlight)
                                .border(1.dp, PriceLensTheme.glassColors.specularBorder, RoundedCornerShape(9999.dp))
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(
                                        if (!uiState.isInspectMode) (if (isDark) BrandMint.copy(alpha = 0.2f) else Color(0xFFE6F4EA))
                                        else Color.Transparent
                                    )
                                    .clickable { viewModel.toggleInspectMode(false) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PanTool,
                                        contentDescription = null,
                                        tint = if (!uiState.isInspectMode) glow else PriceLensTheme.glassColors.textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Browse",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (!uiState.isInspectMode) FontWeight.Bold else FontWeight.Normal,
                                        color = if (!uiState.isInspectMode) glow else PriceLensTheme.glassColors.textSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(if (uiState.isInspectMode) glow else Color.Transparent)
                                    .clickable { viewModel.toggleInspectMode(true) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = null,
                                        tint = if (uiState.isInspectMode) (if (isDark) Color(0xFF000000) else Color.White) else PriceLensTheme.glassColors.textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Select Price",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (uiState.isInspectMode) FontWeight.Bold else FontWeight.Normal,
                                        color = if (uiState.isInspectMode) (if (isDark) Color(0xFF000000) else Color.White) else PriceLensTheme.glassColors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Progress bar
                    if (uiState.isLoadingPage) {
                        LinearProgressIndicator(
                            progress = { uiState.pageLoadProgress / 100f },
                            modifier = Modifier.fillMaxWidth().height(2.dp),
                            color = glow,
                            trackColor = Color.Transparent,
                        )
                    }

                    // Instruction banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (uiState.isInspectMode) (if (isDark) BrandMint.copy(alpha = 0.12f) else Color(0xFFE6F4EA)) else ElectricBlue.copy(alpha = 0.08f)
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.isInspectMode) Icons.Default.TouchApp else Icons.Default.PanTool,
                                contentDescription = null,
                                tint = if (uiState.isInspectMode) glow else ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isInspectMode) {
                                    "Tap on the price displayed on the web page to lock tracking"
                                } else {
                                    "Browse mode: Navigate to the product variant, size, or color first"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                        }
                    }
                }
            }

            // WebView
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            settings.userAgentString =
                                "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

                            addJavascriptInterface(
                                PriceLensWebInterface { rawText, selector, title, url ->
                                    post {
                                        viewModel.onElementSelected(rawText, selector, title, url)
                                    }
                                },
                                "PriceLensBridge"
                            )

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    viewModel.onPageLoadProgress(newProgress)
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    viewModel.onPageLoadProgress(20)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    view?.evaluateJavascript(INSPECTOR_JS) {
                                        val modeStr = if (uiState.isInspectMode) "inspect" else "browse"
                                        view.evaluateJavascript("if (window.priceLensInspector) { window.priceLensInspector.setMode('$modeStr'); }", null)
                                    }
                                    if (url != null) {
                                        viewModel.onPageLoaded(view?.title.orEmpty(), url)
                                    }
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    return false // Load inside WebView
                                }
                            }

                            if (uiState.url.isNotBlank()) {
                                loadUrl(uiState.url)
                            }

                            webViewInstance = this
                        }
                    },
                    update = { view ->
                        webViewInstance = view
                    }
                )
            }
        }

        // Floating Element Confirmation Card at the Bottom
        AnimatedVisibility(
            visible = uiState.selectedRawText != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = PriceLensTheme.glassColors.surfaceGlass,
                border = BorderStroke(1.5.dp, glow),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(glow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isDark) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Selected Price Spot",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PriceLensTheme.glassColors.textPrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.clearSelection()
                                webViewInstance?.evaluateJavascript("if (window.priceLensInspector) { window.priceLensInspector.clearHighlight(); }", null)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = PriceLensTheme.glassColors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Detected Price Display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PriceLensTheme.glassColors.surfaceHighlight)
                            .border(1.dp, PriceLensTheme.glassColors.specularBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Detected on Page:",
                                style = MaterialTheme.typography.labelSmall,
                                color = PriceLensTheme.glassColors.textSecondary
                            )
                            Text(
                                text = uiState.selectedRawText.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PriceLensTheme.glassColors.textPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (uiState.selectedPrice != null) {
                            Text(
                                text = "₹${"%,.0f".format(uiState.selectedPrice)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = glow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Product Name field
                    OutlinedTextField(
                        value = uiState.productNameInput,
                        onValueChange = viewModel::onProductNameChanged,
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PriceLensTheme.glassColors.textPrimary,
                            unfocusedTextColor = PriceLensTheme.glassColors.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Target Price field
                    OutlinedTextField(
                        value = uiState.targetPriceInput,
                        onValueChange = viewModel::onTargetPriceChanged,
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

                    if (!uiState.errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF5252)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearSelection()
                                webViewInstance?.evaluateJavascript("if (window.priceLensInspector) { window.priceLensInspector.clearHighlight(); }", null)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(9999.dp)
                        ) {
                            Text("Reselect Spot")
                        }

                        LiquidGlassPrimaryButton(
                            text = if (uiState.isSaving) "Saving..." else "Track This Price",
                            onClick = viewModel::confirmTrackPrice,
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            }
        }
    }
}

package com.pricelens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.navigation.PriceLensNavGraph
import com.pricelens.core.preferences.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferencesRepository

    private var sharedUrl by mutableStateOf<String?>(null)
    private var sharedProductId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            val themeMode by userPreferences.themeModeFlow.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                UserPreferencesRepository.ThemeMode.DARK -> true
                UserPreferencesRepository.ThemeMode.LIGHT -> false
                UserPreferencesRepository.ThemeMode.SYSTEM -> systemDark
            }

            PriceLensTheme(
                darkTheme = isDark,
                onToggleTheme = {
                    userPreferences.toggleTheme(isDark)
                }
            ) {
                val navController = rememberNavController()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = PriceLensTheme.glassColors.backgroundBrush)
                ) {
                    PriceLensNavGraph(
                        navController = navController,
                        sharedUrl = sharedUrl,
                        sharedProductId = sharedProductId
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
            val urlRegex = Regex("""https?://[^\s]+""")
            val extracted = urlRegex.find(text)?.value ?: text.trim()
            if (extracted.isNotBlank()) {
                sharedUrl = extracted
            }
        } else if (intent.hasExtra("productId")) {
            sharedProductId = intent.getStringExtra("productId")
        }
    }
}

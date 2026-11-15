package com.pricelens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.navigation.PriceLensNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var sharedUrl by mutableStateOf<String?>(null)
    private var sharedProductId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            PriceLensTheme {
                val navController = rememberNavController()
                PriceLensNavGraph(
                    navController = navController,
                    sharedUrl = sharedUrl,
                    sharedProductId = sharedProductId
                )
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

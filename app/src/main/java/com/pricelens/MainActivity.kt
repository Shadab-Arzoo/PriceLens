package com.pricelens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.pricelens.core.designsystem.PriceLensTheme
import com.pricelens.core.navigation.PriceLensNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PriceLensTheme {
                val navController = rememberNavController()
                PriceLensNavGraph(navController = navController)
            }
        }
    }
}

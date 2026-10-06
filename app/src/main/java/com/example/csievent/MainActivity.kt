package com.example.csievent

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.compose.rememberNavController
import com.example.csievent.data.local.AuthEvent
import com.example.csievent.data.local.AuthStateManager
import com.example.csievent.data.local.ThemeMode
import com.example.csievent.data.local.ThemeSettings
import com.example.csievent.presentation.navigation.AppNavGraph
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.CSIEventTheme
import com.example.csievent.ui.theme.Ledger
import com.example.csievent.ui.theme.MidnightGold
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The single Activity that hosts the entire Jetpack Compose UI.
 *
 * - Applies the user's appearance choice (dark / light / follow phone).
 * - Sends the user back to Login when any request returns 401
 *   ([AuthStateManager]), clearing the back stack.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authStateManager: AuthStateManager

    @Inject
    lateinit var themeSettings: ThemeSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        // Draw behind transparent system bars (required from Android 15);
        // screens add their own insets padding
        enableEdgeToEdge(
            statusBarStyle     = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        setContent {
            val mode by themeSettings.mode.collectAsState()
            val darkTheme = when (mode) {
                ThemeMode.DARK   -> true
                ThemeMode.LIGHT  -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // Window colour behind the UI matches the theme (no flash on rotation)
            LaunchedEffect(darkTheme) {
                val bg = if (darkTheme) MidnightGold.background else Ledger.background
                window.setBackgroundDrawable(ColorDrawable(bg.toArgb()))
            }

            CSIEventTheme(darkTheme = darkTheme) {

                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    authStateManager.authEvent.collect { event ->
                        when (event) {
                            is AuthEvent.Unauthorized -> {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                }

                AppNavGraph(navController = navController)
            }
        }
    }
}

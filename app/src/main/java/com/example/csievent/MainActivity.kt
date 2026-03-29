package com.example.csievent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.example.csievent.data.local.AuthEvent
import com.example.csievent.data.local.AuthStateManager
import com.example.csievent.presentation.navigation.AppNavGraph
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.CSIEventTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The single Activity that hosts the entire Jetpack Compose UI.
 *
 * Responsibilities:
 * 1. Sets up the Compose content with the app theme.
 * 2. Creates the [NavHostController] used by [AppNavGraph].
 * 3. Listens for [AuthEvent.Unauthorized] from [AuthStateManager] and
 *    navigates the user back to Login when their JWT token expires.
 *
 * FIX: Added 401 auto-logout observer. Previously, when a JWT token expired,
 * every screen would show a generic error with no way to recover. Now, the
 * moment a 401 is received anywhere in the app, the user is immediately
 * redirected to the Login screen.
 *
 * Reference — Hilt with Android Activities:
 * https://developer.android.com/training/dependency-injection/hilt-android#android-classes
 *
 * Reference — Collecting flows in Compose:
 * https://developer.android.com/develop/ui/compose/side-effects#launchedeffect
 *
 * File: app/src/main/java/com/example/csievent/MainActivity.kt
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Injected by Hilt. Provides the [AuthEvent] flow that [AuthInterceptor]
     * emits to when a 401 is received.
     */
    @Inject
    lateinit var authStateManager: AuthStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CSIEventTheme {

                val navController = rememberNavController()

                // ------------------------------------------------------------------
                // Observe 401 events from AuthStateManager.
                //
                // LaunchedEffect(Unit) starts a coroutine that collects the
                // SharedFlow for the lifetime of this composable (the whole app).
                //
                // When Unauthorized is received:
                //   - Navigate to Login
                //   - Clear the entire back stack (popUpTo 0) so the user
                //     cannot press Back to return to a protected screen
                // ------------------------------------------------------------------
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
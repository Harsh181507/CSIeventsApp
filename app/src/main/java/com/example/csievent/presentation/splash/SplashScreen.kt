package com.example.csievent.presentation.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.R
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.CsiTheme
import kotlinx.coroutines.delay

/**
 * Logo with a short fade-in while the saved login is checked. Opens the
 * right home screen as soon as that check is done (min. ~0.7 s so the
 * logo doesn't flicker).
 */
@Composable
fun SplashScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(450), label = "splash")

    LaunchedEffect(Unit) {
        visible = true
        val started = System.currentTimeMillis()
        val role = viewModel.resolveRole()
        val elapsed = System.currentTimeMillis() - started
        if (elapsed < MIN_SPLASH_MS) delay(MIN_SPLASH_MS - elapsed)

        navController.navigate(Routes.dashboardFor(role) ?: Routes.LOGIN) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.csi_logo),
                contentDescription = "CSI VIT-AP",
                modifier = Modifier.size(128.dp)
            )
            Text("CSI Events", style = MaterialTheme.typography.headlineMedium, color = c.text)
            Text("CSI VIT-AP", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
        }
    }
}

private const val MIN_SPLASH_MS = 700L

package com.example.csievent.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.R
import com.example.csievent.data.remote.api.ApiConstants
import com.example.csievent.presentation.components.CsiTextField
import com.example.csievent.presentation.components.GhostButton
import com.example.csievent.presentation.components.NoticeBanner
import com.example.csievent.presentation.components.PrimaryButton
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.CsiTheme

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()
    val focus = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.role) {
        state.role?.let { role ->
            navController.navigate(Routes.dashboardFor(role) ?: Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    fun submit() {
        focus.clearFocus()
        viewModel.login(email, password)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.csi_logo),
                contentDescription = "CSI VIT-AP",
                modifier = Modifier.size(104.dp)
            )
            Text("CSI Events", style = MaterialTheme.typography.displaySmall, color = c.text)
            Text(
                "Sign in to join your team and follow the scores.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMuted,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(8.dp))

        CsiTextField(
            value = email,
            onValueChange = { email = it },
            label = "College email",
            placeholder = "you@vitapstudent.ac.in",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
        CsiTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeAction = ::submit
        )

        state.error?.let { NoticeBanner(text = it, icon = Icons.Rounded.ErrorOutline, danger = true) }

        PrimaryButton(
            text = "Sign in",
            onClick = ::submit,
            loading = state.isLoading,
            enabled = email.isNotBlank() && password.isNotBlank()
        )

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("New here?", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
            GhostButton(text = "Create an account", color = c.highlight, onClick = { navController.navigate(Routes.REGISTER) })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("You stay signed in for 7 days ·", style = MaterialTheme.typography.bodySmall, color = c.textSubtle)
            GhostButton(text = "Privacy", onClick = { uriHandler.openUri(ApiConstants.PRIVACY_POLICY_URL) })
        }
    }
}

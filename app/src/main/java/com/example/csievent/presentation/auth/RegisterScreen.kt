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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
fun RegisterScreen(
    navController: NavHostController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()
    val focus = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    var name by rememberSaveable { mutableStateOf("") }
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
        viewModel.register(name, email, password)
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.csi_logo),
                contentDescription = "CSI VIT-AP",
                modifier = Modifier.size(56.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Create your account", style = MaterialTheme.typography.headlineMedium, color = c.text)
            Text(
                "Use your college email. Organizers can make you a judge later.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMuted
            )
        }
        Spacer(Modifier.height(4.dp))

        CsiTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full name",
            placeholder = "Aarav Kumar",
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        )
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
            supportingText = "At least 6 characters",
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeAction = ::submit
        )

        state.error?.let { NoticeBanner(text = it, icon = Icons.Rounded.ErrorOutline, danger = true) }

        PrimaryButton(
            text = "Create account",
            onClick = ::submit,
            loading = state.isLoading,
            enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account?", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
            GhostButton(
                text = "Sign in",
                color = c.highlight,
                onClick = { if (!navController.popBackStack()) navController.navigate(Routes.LOGIN) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("By signing up you agree to the", style = MaterialTheme.typography.bodySmall, color = c.textSubtle)
            GhostButton(text = "Privacy policy", onClick = { uriHandler.openUri(ApiConstants.PRIVACY_POLICY_URL) })
        }
    }
}

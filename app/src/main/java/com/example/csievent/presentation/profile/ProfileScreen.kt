package com.example.csievent.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.local.ThemeMode
import com.example.csievent.data.remote.api.ApiConstants
import com.example.csievent.presentation.components.Avatar
import com.example.csievent.presentation.components.ChipKind
import com.example.csievent.presentation.components.CsiCard
import com.example.csievent.presentation.components.CsiScreen
import com.example.csievent.presentation.components.CsiTextField
import com.example.csievent.presentation.components.CsiTopBar
import com.example.csievent.presentation.components.SectionHeader
import com.example.csievent.presentation.components.SegmentedTabs
import com.example.csievent.presentation.components.StatusChip
import com.example.csievent.presentation.navigation.Routes
import com.example.csievent.ui.theme.ControlShape
import com.example.csievent.ui.theme.CsiTheme

/**
 * Account screen for every role: who is logged in, appearance, privacy
 * policy, logout and permanent account deletion (Google Play requires
 * in-app deletion for apps that let users create accounts).
 */
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val c = CsiTheme.colors
    val state by viewModel.state.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val uriHandler = LocalUriHandler.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.signedOut) {
        if (state.signedOut) {
            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }
    }

    CsiScreen(
        topBar = { CsiTopBar(title = "Profile", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CsiCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Avatar(state.name.ifBlank { "?" }, size = 64, emphasized = true)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(state.name.ifBlank { "—" }, style = MaterialTheme.typography.titleLarge, color = c.text)
                        Text(state.email, style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                        if (state.role.isNotBlank()) {
                            StatusChip(state.role.lowercase().replaceFirstChar { it.uppercase() }, ChipKind.HIGHLIGHT)
                        }
                    }
                }
            }

            SectionHeader("Appearance")
            SegmentedTabs(
                options = listOf("Dark", "Light", "Phone setting"),
                selected = when (themeMode) {
                    ThemeMode.DARK -> 0
                    ThemeMode.LIGHT -> 1
                    ThemeMode.SYSTEM -> 2
                },
                onSelect = { index ->
                    viewModel.setThemeMode(
                        when (index) {
                            0 -> ThemeMode.DARK
                            1 -> ThemeMode.LIGHT
                            else -> ThemeMode.SYSTEM
                        }
                    )
                }
            )

            SectionHeader("Account", modifier = Modifier.padding(top = 8.dp))
            CsiCard(padding = 4) {
                ActionRow(Icons.Rounded.Policy, "Privacy policy", c.text) {
                    uriHandler.openUri(ApiConstants.PRIVACY_POLICY_URL)
                }
                ActionRow(Icons.AutoMirrored.Rounded.Logout, "Log out", c.text) {
                    viewModel.logout()
                }
                ActionRow(Icons.Rounded.DeleteForever, "Delete account", c.danger) {
                    viewModel.clearError()
                    showDeleteDialog = true
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "CSI Events · CSI VIT-AP",
                style = MaterialTheme.typography.bodySmall,
                color = c.textSubtle,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            isDeleting = state.isDeleting,
            error = state.error,
            onConfirm = { password -> viewModel.deleteAccount(password) },
            onDismiss = {
                if (!state.isDeleting) {
                    showDeleteDialog = false
                    viewModel.clearError()
                }
            }
        )
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    val c = CsiTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ControlShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Text(label, style = MaterialTheme.typography.titleSmall, color = color, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = c.textSubtle)
    }
}

@Composable
private fun DeleteAccountDialog(
    isDeleting: Boolean,
    error: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val c = CsiTheme.colors
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text("Delete your account?", style = MaterialTheme.typography.titleLarge, color = c.text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "This permanently deletes your account, your team memberships and any scores you gave. " +
                        "Teams you lead are passed to another member. This can't be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMuted
                )
                CsiTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true,
                    enabled = !isDeleting,
                    imeAction = ImeAction.Done,
                    onImeAction = { onConfirm(password) },
                    error = error
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }, enabled = !isDeleting) {
                if (isDeleting) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = c.danger, strokeWidth = 2.dp)
                } else {
                    Text("Delete permanently", color = c.danger, style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text("Cancel", color = c.textMuted, style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

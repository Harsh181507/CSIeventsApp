package com.example.csievent.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csievent.data.remote.api.ApiConstants
import com.example.csievent.presentation.navigation.Routes

private val Bg        = Color(0xFF05040D)
private val Card      = Color(0xFF0D0B1F)
private val Line      = Color(0xFF2A2650)
private val Accent    = Color(0xFF8C83E4)
private val Danger    = Color(0xFFEF4444)
private val TextMain  = Color(0xFFECEBF7)
private val TextMuted = Color(0xFF9C99B8)

/**
 * Account screen for every role: who is logged in, privacy policy, logout and
 * permanent account deletion (Google Play requires in-app deletion for apps
 * that let users create accounts).
 *
 * File: app/src/main/java/com/example/csievent/presentation/profile/ProfileScreen.kt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.signedOut) {
        if (state.signedOut) {
            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }
    }

    Scaffold(
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = {
                    Text("PROFILE", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Avatar + identity ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF4B3CC8))))
                    .border(2.dp, Accent.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initials(state.name),
                    color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(state.name.ifBlank { "—" }, color = TextMain, fontSize = 22.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(state.email, color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)

            if (state.role.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Accent.copy(alpha = 0.15f))
                        .border(1.dp, Accent.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(state.role, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Actions ────────────────────────────────────────────────
            ActionRow(Icons.Default.Policy, "Privacy policy", TextMain) {
                uriHandler.openUri(ApiConstants.PRIVACY_POLICY_URL)
            }
            Spacer(Modifier.height(10.dp))
            ActionRow(Icons.Default.ExitToApp, "Log out", TextMain) {
                viewModel.logout()
            }
            Spacer(Modifier.height(10.dp))
            ActionRow(Icons.Default.DeleteForever, "Delete account", Danger) {
                viewModel.clearError()
                showDeleteDialog = true
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "CSI Events",
                color = TextMuted.copy(alpha = 0.6f), fontSize = 12.sp
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Card)
            .border(1.dp, if (color == Danger) Danger.copy(alpha = 0.4f) else Line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Spacer(Modifier.width(14.dp))
        Text(label, color = color, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DeleteAccountDialog(
    isDeleting: Boolean,
    error: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Card,
        title = { Text("Delete your account?", color = TextMain) },
        text = {
            Column {
                Text(
                    "This permanently deletes your account, your team memberships and any scores you gave. " +
                        "Teams you lead are passed to another member. This can't be undone.",
                    color = TextMuted, fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    enabled = !isDeleting,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (visible) "Hide password" else "Show password",
                                tint = TextMuted
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain,
                        focusedBorderColor = Danger,
                        unfocusedBorderColor = Line,
                        focusedLabelColor = Danger,
                        unfocusedLabelColor = TextMuted,
                        cursorColor = Danger
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Danger, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }, enabled = !isDeleting) {
                if (isDeleting) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = Danger, strokeWidth = 2.dp)
                } else {
                    Text("Delete permanently", color = Danger, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text("Cancel", color = Accent)
            }
        }
    )
}

private fun initials(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

package com.example.csievent.presentation.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.csievent.ui.theme.CsiTheme

/**
 * Confirmation dialog for destructive or important actions.
 * [destructive] colours the confirm button red.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false
) {
    val c = CsiTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        titleContentColor = c.text,
        textContentColor = c.textMuted,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) {
                Text(confirmLabel, style = MaterialTheme.typography.labelLarge, color = if (destructive) c.danger else c.highlight)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = c.textMuted)
            }
        }
    )
}

/**
 * Dialog around form fields ([content]). Stays open while [working] and shows
 * [error] under the fields; the caller closes it once the save succeeds.
 */
@Composable
fun FormDialog(
    title: String,
    confirmLabel: String,
    working: Boolean,
    error: String?,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = CsiTheme.colors
    AlertDialog(
        onDismissRequest = { if (!working) onDismiss() },
        containerColor = c.surface,
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = c.text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                content()
                error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = c.danger) }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = confirmEnabled && !working) {
                if (working) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = c.highlight, strokeWidth = 2.dp)
                } else {
                    Text(
                        confirmLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (confirmEnabled) c.highlight else c.textSubtle
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !working) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = c.textMuted)
            }
        }
    )
}

/**
 * Shows [message] in the snackbar once, then calls [onShown] so the
 * ViewModel can clear it.
 */
@Composable
fun MessageEffect(message: String?, snackbar: SnackbarHostState, onShown: () -> Unit) {
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
            onShown()
        }
    }
}

/** Opens the Android share sheet with [text]. */
fun shareText(context: Context, text: String, title: String = "Share") {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** Android 13+ shows its own "copied" preview, so only older versions need our message. */
fun shouldConfirmCopy(): Boolean = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU

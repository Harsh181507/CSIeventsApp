package com.example.csievent.presentation.components

import android.content.Context
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

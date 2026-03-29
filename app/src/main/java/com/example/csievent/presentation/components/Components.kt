package com.example.csievent.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.csievent.ui.theme.BadgeShape
import com.example.csievent.ui.theme.ButtonShape
import com.example.csievent.ui.theme.CardShape
import com.example.csievent.ui.theme.InputShape
import com.example.csievent.ui.theme.PillShape

// =============================================================================
// APP BUTTON
// =============================================================================

/**
 * Primary button — full-width, indigo background.
 * Used for main actions: "Create Team", "Submit Score", "Login".
 */
@Composable
fun AppButton(
    text:     String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
    enabled:  Boolean  = true,
    loading:  Boolean  = false
) {
    Button(
        onClick  = onClick,
        enabled  = enabled && !loading,
        shape    = ButtonShape,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = MaterialTheme.colorScheme.primary,
            contentColor           = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        AnimatedContent(
            targetState = loading,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "button_content"
        ) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier  = Modifier.size(20.dp),
                    color     = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text       = text,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Outlined secondary button.
 * Used for secondary actions: "Cancel", "Back", "Join with Code".
 */
@Composable
fun AppOutlinedButton(
    text:     String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
    enabled:  Boolean  = true
) {
    OutlinedButton(
        onClick  = onClick,
        enabled  = enabled,
        shape    = ButtonShape,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        border = ButtonDefaults.outlinedButtonBorder,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

// =============================================================================
// APP TEXT FIELD
// =============================================================================

/**
 * Styled text input field.
 * Used consistently across all forms in the app.
 */
@Composable
fun AppTextField(
    value:               String,
    onValueChange:       (String) -> Unit,
    label:               String,
    modifier:            Modifier            = Modifier,
    placeholder:         String              = "",
    isError:             Boolean             = false,
    errorMessage:        String?             = null,
    singleLine:          Boolean             = true,
    keyboardOptions:     KeyboardOptions     = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon:        @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value                = value,
            onValueChange        = onValueChange,
            label                = {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            placeholder          = if (placeholder.isNotBlank()) {
                { Text(placeholder, style = MaterialTheme.typography.bodyMedium) }
            } else null,
            isError              = isError,
            singleLine           = singleLine,
            keyboardOptions      = keyboardOptions,
            visualTransformation = visualTransformation,
            trailingIcon         = trailingIcon,
            shape                = InputShape,
            modifier             = Modifier.fillMaxWidth(),
            colors               = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor     = MaterialTheme.colorScheme.error,
                focusedLabelColor    = MaterialTheme.colorScheme.primary,
                cursorColor          = MaterialTheme.colorScheme.primary
            )
        )

        // Inline error message
        AnimatedVisibility(visible = isError && errorMessage != null) {
            errorMessage?.let { message ->
                Text(
                    text     = message,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }
    }
}

// =============================================================================
// FEEDBACK BANNERS
// =============================================================================

/**
 * Animated error banner shown at the bottom of screens.
 * Auto-dismissable by tapping the Dismiss button.
 */
@Composable
fun ErrorBanner(
    message:   String,
    onDismiss: () -> Unit,
    modifier:  Modifier = Modifier
) {
    AnimatedVisibility(
        visible  = true,
        enter    = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit     = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape    = CardShape,
            color    = MaterialTheme.colorScheme.errorContainer,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier          = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text     = message,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = onDismiss,
                    colors  = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text  = "Dismiss",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Animated success banner shown at the bottom of screens.
 */
@Composable
fun SuccessBanner(
    message:   String,
    onDismiss: () -> Unit,
    modifier:  Modifier = Modifier
) {
    AnimatedVisibility(
        visible  = true,
        enter    = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit     = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = CardShape,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier          = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text     = message,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = onDismiss,
                    colors  = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Text(
                        text  = "Dismiss",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// =============================================================================
// LOADING OVERLAY
// =============================================================================

/**
 * Full-screen loading overlay with a centered spinner.
 * Used when the entire screen content is loading.
 */
@Composable
fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color       = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier    = Modifier.size(48.dp)
        )
    }
}

// =============================================================================
// SECTION HEADER
// =============================================================================

/**
 * Section divider header with a title and optional count badge.
 * Used in lists to group items (e.g. "Judges (3)", "Students (12)").
 */
@Composable
fun SectionHeader(
    title:    String,
    count:    Int?    = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text     = title,
            style    = MaterialTheme.typography.titleSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )

        count?.let { n ->
            Surface(
                shape = PillShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text     = n.toString(),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Divider(
            modifier  = Modifier.weight(1f),
            color     = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
    }
}

// =============================================================================
// ROLE BADGE
// =============================================================================

/**
 * Colored pill badge showing a user's role.
 * ORGANIZER = primary, JUDGE = tertiary (cyan), STUDENT = surface variant.
 */
@Composable
fun RoleBadge(
    role:     String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (role.uppercase()) {
        "ORGANIZER" -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        "JUDGE" -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        shape    = PillShape,
        color    = bgColor,
        modifier = modifier
    ) {
        Text(
            text     = role,
            style    = MaterialTheme.typography.labelSmall,
            color    = textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// =============================================================================
// EMPTY STATE
// =============================================================================

/**
 * Centered empty state with an emoji icon and message.
 * Used when a list has no items.
 */
@Composable
fun EmptyState(
    icon:     String,
    title:    String,
    subtitle: String    = "",
    modifier: Modifier  = Modifier
) {
    Column(
        modifier            = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text  = icon,
            style = MaterialTheme.typography.displaySmall
        )

        Text(
            text      = title,
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onSurface
        )

        if (subtitle.isNotBlank()) {
            Text(
                text      = subtitle,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
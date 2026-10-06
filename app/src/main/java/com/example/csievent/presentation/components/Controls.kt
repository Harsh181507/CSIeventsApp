package com.example.csievent.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csievent.ui.theme.ControlShape
import com.example.csievent.ui.theme.CsiTheme
import com.example.csievent.ui.theme.SpaceGrotesk
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle

// =============================================================================
// BUTTONS
// =============================================================================

/** Main action: gold in dark mode, ink in light mode. Shows a spinner while [loading]. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    val c = CsiTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = ControlShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = c.accent,
            contentColor = c.onAccent,
            disabledContainerColor = if (loading) c.accent else c.line,
            disabledContentColor = if (loading) c.onAccent else c.textSubtle
        ),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = c.onAccent,
                strokeWidth = 2.5.dp
            )
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = buttonTextStyle())
        }
    }
}

/** Secondary action: outlined. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    contentColor: Color = CsiTheme.colors.text,
    height: Int = 54
) {
    val c = CsiTheme.colors
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height.dp),
        shape = ControlShape,
        border = BorderStroke(1.dp, if (enabled) c.line else c.line.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = c.surface,
            contentColor = contentColor,
            disabledContentColor = c.textSubtle
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Low-emphasis text action. */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = CsiTheme.colors.textMuted,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 44.dp),
        shape = ControlShape
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = if (enabled) color else CsiTheme.colors.textSubtle)
    }
}

/** Square icon button with a visible outline (copy, share, +/−). */
@Composable
fun OutlinedIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    enabled: Boolean = true
) {
    val c = CsiTheme.colors
    val bg = if (filled) c.accent else c.surface
    val fg = if (filled) c.onAccent else c.text
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(ControlShape)
            .background(if (enabled) bg else c.raised)
            .then(if (filled) Modifier else Modifier.border(1.dp, c.line, ControlShape)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(icon, contentDescription = contentDescription, tint = if (enabled) fg else c.textSubtle)
        }
    }
}

/** Round initials button used for the profile entry in top bars. */
@Composable
fun AvatarButton(name: String, onClick: () -> Unit) {
    val c = CsiTheme.colors
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(44.dp)
            .clip(CircleShape)
            .background(c.raised)
            .border(1.dp, c.line, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Text(
                initials(name),
                style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                color = c.highlight
            )
        }
    }
}

@Composable
private fun buttonTextStyle() = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 17.sp)

// =============================================================================
// TEXT FIELDS
// =============================================================================

/**
 * Labelled text field. Label sits above the box (clearer than floating labels).
 * [isPassword] adds a show/hide toggle.
 */
@Composable
fun CsiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    supportingText: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    trailing: (@Composable () -> Unit)? = null
) {
    val c = CsiTheme.colors
    var visible by rememberSaveable { mutableStateOf(false) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textMuted)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            minLines = minLines,
            textStyle = textStyle,
            isError = error != null,
            placeholder = placeholder?.let { { Text(it, style = textStyle, color = c.textSubtle) } },
            visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                capitalization = capitalization,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(onAny = { onImeAction?.invoke() }),
            trailingIcon = when {
                isPassword -> {
                    {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (visible) "Hide password" else "Show password",
                                tint = c.textMuted
                            )
                        }
                    }
                }
                trailing != null -> trailing
                else -> null
            },
            shape = ControlShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = c.text,
                unfocusedTextColor = c.text,
                disabledTextColor = c.textMuted,
                focusedContainerColor = c.surface,
                unfocusedContainerColor = c.surface,
                disabledContainerColor = c.raised,
                errorContainerColor = c.surface,
                focusedBorderColor = c.highlight,
                unfocusedBorderColor = c.line,
                disabledBorderColor = c.line,
                errorBorderColor = c.danger,
                cursorColor = c.highlight,
                errorCursorColor = c.danger
            )
        )
        when {
            error != null -> Text(error, style = MaterialTheme.typography.bodySmall, color = c.danger)
            supportingText != null -> Text(supportingText, style = MaterialTheme.typography.bodySmall, color = c.textSubtle)
        }
    }
}

// =============================================================================
// SEGMENTED CONTROL
// =============================================================================

/** Two-to-four option switch (e.g. Upcoming / Past). */
@Composable
fun SegmentedTabs(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = CsiTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ControlShape)
            .background(c.surface)
            .border(1.dp, c.line, ControlShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            TextButton(
                onClick = { onSelect(index) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = ControlShape,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = if (isSelected) c.raised else Color.Transparent,
                    contentColor = if (isSelected) c.text else c.textMuted
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
            }
        }
    }
}

// =============================================================================
// PICKER FIELD
// =============================================================================

/**
 * Looks like a text field but opens a picker (e.g. a date) when tapped.
 * Exposed to screen readers as a button with its label and current value.
 */
@Composable
fun PickerField(
    label: String,
    value: String?,
    placeholder: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = CsiTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textMuted)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(ControlShape)
                .background(c.surface)
                .border(1.dp, c.line, ControlShape)
                .clickable(role = Role.Button, onClickLabel = "Change $label", onClick = onClick)
                .semantics(mergeDescendants = true) { contentDescription = "$label, ${value ?: placeholder}" }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                value ?: placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = if (value == null) c.textSubtle else c.text,
                modifier = Modifier.weight(1f)
            )
            Icon(icon, contentDescription = null, tint = c.textMuted)
        }
    }
}

package com.example.csievent.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csievent.ui.theme.CardShape
import com.example.csievent.ui.theme.CsiTheme
import com.example.csievent.ui.theme.PillShape
import com.example.csievent.ui.theme.SpaceGrotesk
import androidx.compose.ui.text.TextStyle

// =============================================================================
// CARDS
// =============================================================================

/**
 * Plain surface card. [highlighted] gives it the gold outline used for
 * "your team" / "happening next" items.
 */
@Composable
fun CsiCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    padding: Int = 16,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = CsiTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(c.surface)
            .border(1.dp, if (highlighted) c.highlightBorder else if (c.isDark) Color.Transparent else c.line, CardShape)
            .then(
                if (onClick != null) Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                else Modifier
            )
            .padding(padding.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

// =============================================================================
// CHIPS
// =============================================================================

enum class ChipKind { SUCCESS, HIGHLIGHT, OUTLINE, NEUTRAL, DANGER }

/** Small status label: "You're in", "Open", "Results", "Locked". */
@Composable
fun StatusChip(text: String, kind: ChipKind, modifier: Modifier = Modifier) {
    val c = CsiTheme.colors
    val (bg, fg, border) = when (kind) {
        ChipKind.SUCCESS -> Triple(c.successContainer, c.success, Color.Transparent)
        ChipKind.HIGHLIGHT -> Triple(c.highlightContainer, c.onHighlightContainer, Color.Transparent)
        ChipKind.OUTLINE -> Triple(Color.Transparent, c.highlight, c.highlightBorder)
        ChipKind.NEUTRAL -> Triple(c.raised, c.textMuted, Color.Transparent)
        ChipKind.DANGER -> Triple(c.dangerContainer, c.danger, Color.Transparent)
    }
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        maxLines = 1,
        modifier = modifier
            .clip(PillShape)
            .background(bg)
            .border(1.dp, border, PillShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

// =============================================================================
// TEXT HELPERS
// =============================================================================

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: String? = null) {
    val c = CsiTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.text)
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.labelMedium, color = c.textMuted)
        }
    }
}

/** Uppercase small caption above a value ("TEAM JOIN CODE"). */
@Composable
fun Caption(text: String, color: Color = CsiTheme.colors.textMuted) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp), color = color)
}

/** Round initials avatar. */
@Composable
fun Avatar(name: String, size: Int = 36, emphasized: Boolean = false) {
    val c = CsiTheme.colors
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(if (emphasized) c.highlightContainer else c.raised),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials(name),
            style = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = (size * 0.36f).sp),
            color = if (emphasized) c.onHighlightContainer else c.text
        )
    }
}

/** Thin progress bar (e.g. teams scored). */
@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier, color: Color = CsiTheme.colors.highlight) {
    val c = CsiTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(c.line)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
    }
}

/** Big stat number with caption, used in small tiles. */
@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    val c = CsiTheme.colors
    Column(
        modifier = modifier
            .clip(CardShape)
            .background(c.surface)
            .border(1.dp, if (c.isDark) Color.Transparent else c.line, CardShape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = c.text)
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// =============================================================================
// SCREEN STATES
// =============================================================================

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CsiTheme.colors.highlight, strokeWidth = 3.dp)
    }
}

/** Friendly empty/info state with an optional action. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val c = CsiTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(c.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = c.highlight, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.text, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = c.textMuted, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(6.dp))
            SecondaryButton(text = actionLabel, onClick = onAction, height = 48)
        }
    }
}

/** Error with retry, for screens that failed to load. */
@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Rounded.CloudOff,
        title = "Couldn't load this",
        message = message,
        modifier = modifier,
        actionLabel = "Try again",
        onAction = onRetry
    )
}

/** Inline notice row (info / warning) inside a screen. */
@Composable
fun NoticeBanner(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val c = CsiTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(if (danger) c.dangerContainer else c.highlightContainer)
            .then(if (onAction != null) Modifier.clickable(onClick = onAction) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = if (danger) c.danger else c.onHighlightContainer, modifier = Modifier.size(20.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (danger) c.danger else c.onHighlightContainer,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null) {
            Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = if (danger) c.danger else c.onHighlightContainer)
        }
    }
}

/** Gap helper for lists built with Column. */
@Composable
fun VSpace(height: Int) = Spacer(Modifier.height(height.dp))

@Composable
fun HSpace(width: Int) = Spacer(Modifier.width(width.dp))

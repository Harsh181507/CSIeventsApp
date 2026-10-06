package com.example.csievent.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csievent.ui.theme.ControlShape
import com.example.csievent.ui.theme.CsiTheme
import com.example.csievent.ui.theme.monoStyle

/** One event in a list: title, detail line and a status chip. */
@Composable
fun EventRow(
    title: String,
    detail: String,
    chipText: String?,
    chipKind: ChipKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    val c = CsiTheme.colors
    CsiCard(modifier = modifier, onClick = onClick, highlighted = highlighted) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = c.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (chipText != null) StatusChip(chipText, chipKind)
        }
    }
}

/** Team join code with copy and share buttons. */
@Composable
fun JoinCodeBox(
    code: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = CsiTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ControlShape)
            .background(c.background)
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Caption("Team join code")
            Text(formatJoinCode(code), style = monoStyle(22.sp, letterSpacing = 2.sp), color = c.highlight)
        }
        OutlinedIconButton(icon = Icons.Rounded.ContentCopy, contentDescription = "Copy join code", onClick = onCopy)
        OutlinedIconButton(icon = Icons.Rounded.Share, contentDescription = "Share join code", onClick = onShare, filled = true)
    }
}
